#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include "path.h"

static double angle1(Turn *turn);
static double angle2(Turn *turn);

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
  double arcs = turn->turn * (angle2(turn) - angle1(turn));
  if (arcs < 0) arcs += 2 * M_PI;
  return turn->circle.radius * arcs;
}

/**
 * Fly a given distance along the path
 */
Path *turn_fly(Turn *turn, const double distance) {
  if (distance < 0) {
    printf("Flight distance cannot be negative %f", distance);
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

Point *turn_render(Turn *turn) {
  const double a1 = angle1(turn);
  const double a2 = angle2(turn);
  const int n = 10; // TODO: Pre-determine based on length
  Point *points = new Point[n];
  const double step = 0.1; // ~6 degrees
  double arcs = turn->turn * (a2 - a1);
  if (arcs < 0) arcs += 2 * M_PI;
  for (int i = 0; i < n - 1; i++) {
    const double delta = i * step;
    const double theta = a1 + turn->turn * delta;
    points[i].x = turn->circle.x + turn->circle.radius * sin(theta);
    points[i].y = turn->circle.y + turn->circle.radius * cos(theta);
  }
  points[n - 1] = turn->end;
  return points;
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
