#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include "geo.h"
#include "path.h"

static double angle1(Turn *turn);
static double angle2(Turn *turn);
static double arcs(Turn *turn);

// If turn is less than minimum_turn, then don't bury a toggle
const float minimum_turn = to_radians(8);
// If turn is at least maximum_turn, then do bury a toggle
const float maximum_turn = to_radians(30);

PointV turn_start(Turn *turn) {
  const double dx = turn->start.x - turn->circle.x;
  const double dy = turn->start.y - turn->circle.y;
  return PointV {turn->start.x, turn->start.y, dx / turn->circle.radius, dy / turn->circle.radius};
}

PointV turn_end(Turn *turn) {
  const double dx = turn->end.x - turn->circle.x;
  const double dy = turn->end.y - turn->circle.y;
  return PointV {turn->end.x, turn->end.y, dx / turn->circle.radius, dy / turn->circle.radius};
}

double turn_length(Turn *turn) {
  double arc = turn->turn * (angle2(turn) - angle1(turn));
  if (arc < 0) arc += 2 * M_PI;
  return turn->circle.radius * arc;
}

/**
 * Fly a given distance along the path
 */
Path *turn_fly(Turn *turn, const double distance) {
  if (distance < 0) {
    printf("segment_turn distance must be positive %f\n", distance);
  }
  const double len = turn_length(turn);
  if (distance < len) {
    const double theta = angle1(turn) + turn->turn * distance / turn->circle.radius;
    Point point = {
      turn->circle.x + turn->circle.radius * sin(theta),
      turn->circle.y + turn->circle.radius * cos(theta)
    };
    Turn *proj = new Turn {
      'T',
      turn->start,
      point,
      turn->circle,
      turn->turn
    };
    return new_path("turn-fly1", 1, (Segment**) &proj);
  } else {
    // Line extending from end of this turn
    const double remaining = distance - len;
    const double dx = turn->end.x - turn->circle.x;
    const double dy = turn->end.y - turn->circle.y;
    Point extension = {
      turn->end.x + turn->turn * remaining * dy / turn->circle.radius,
      turn->end.y - turn->turn * remaining * dx / turn->circle.radius
    };
    Line *line = new Line {'L', turn->end, extension};
    Segment *segments[] = {
      segment_copy((Segment*) turn),
      (Segment*) line
    };
    return new_path("turn-fly2", 2, segments);
  }
}

// Point *turn_render(Turn *turn) {
//   const double a1 = angle1(turn);
//   const double a2 = angle2(turn);
//   const double step = 0.1; // ~6 degrees

//   // Arc length
//   double arc = turn->turn * (a2 - a1);
//   if (arc < 0) arc += 2 * M_PI;

//   const int n = ceil(arc / step) + 1;
//   Point *points = new Point[n];
//   points[0] = turn->start;

//   for (int i = 1; i < n - 1; i++) {
//     const double delta = i * step;
//     const double theta = a1 + turn->turn * delta;
//     points[i].x = turn->circle.x + turn->circle.radius * sin(theta);
//     points[i].y = turn->circle.y + turn->circle.radius * cos(theta);
//   }
//   points[n - 1] = turn->end;
//   return points;
// }

/**
 * Return target toggle position for a given turn.
 * If the turn is short, don't crank a hard toggle turn.
 */
TogglePosition turn_controls(Turn *turn) {
  const float arc = arcs(turn);
  float activation = 1;
  if (arc < minimum_turn) {
    // 0..min_turn
    activation = 0.01f * arc / minimum_turn;
  } else if (arc < maximum_turn) {
    // min_turn..max_turn
    activation = 0.01f + 0.99f * (arc - minimum_turn) / (maximum_turn - minimum_turn);
  }
  const uint8_t deflect = activation * 255;
  TogglePosition toggles = {};
  if (turn->turn == TURN_RIGHT) {
    toggles.right = deflect;
  } else {
    toggles.left = deflect;
  }
  return toggles;
}

/**
 * The arc angle in radians
 */
static double arcs(Turn *turn) {
  if (turn->start.x == turn->end.x && turn->start.y == turn->end.y) {
    return 0;
  }
  double arc = turn->turn * (angle2(turn) - angle1(turn));
  if (arc < 0) arc += 2 * M_PI;
  return arc;
}

/**
 * Angle from center of circle to start
 */
static double angle1(Turn *turn) {
  return atan2(turn->start.x - turn->circle.x, turn->start.y - turn->circle.y);
}

/**
 * Angle from center of circle to end
 */
static double angle2(Turn *turn) {
  return atan2(turn->end.x - turn->circle.x, turn->end.y - turn->circle.y);
}
