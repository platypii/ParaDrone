#include <math.h>
#include <stdlib.h>
#include "path.h"

/**
 * Fly naively to a waypoint.
 * This path will consist of a turn, plus a straight line to the target.
 * You will probably not arrive at your destination in the DIRECTION you want though.
 */
Path *naive(PointV loc, PointV dest, double r) {
  const double velocity = sqrt(loc.vx * loc.vx + loc.vy * loc.vy);
  if (velocity == 0) {
    // printf("Zero velocity no tangent");
    return NULL;
  }
  const double delta_x = dest.x - loc.x;
  const double delta_y = dest.y - loc.y;
  const double delta = sqrt(delta_x * delta_x + delta_y * delta_y);
  if (delta < 2 * r) {
    // printf("Naive planner on top of lz");
    return NULL;
  }
  // Is dest on our left or right?
  const double dot = delta_y * loc.vx - delta_x * loc.vy;
  const int turn1 = dot > 0 ? TURN_LEFT : TURN_RIGHT;
  Circle c1 = {
    .x = loc.x + turn1 * r * loc.vy / velocity,
    .y = loc.y - turn1 * r * loc.vx / velocity,
    .radius = r
  };
  // Angle from circle center to target
  const double center_angle = atan2(dest.x - c1.x, dest.y - c1.y);
  // Commute
  const double cdx = c1.x - dest.x;
  const double cdy = c1.y - dest.y;
  const double cdest = sqrt(cdx * cdx + cdy * cdy);
  const double offset = turn1 * asin(r / cdest);
  const double commute_angle = center_angle + offset;
  // const double commute_length = sqrt(cdest * cdest - r * r);
  // Last touch of first dubin circle (start of commute home)
  Point comm1 = {
    x: c1.x - turn1 * r * cos(commute_angle),
    y: c1.y + turn1 * r * sin(commute_angle)
  };
  // Construct path
  Turn *arc1 = new Turn {'T', {loc.x, loc.y}, comm1, c1, turn1};
  Line *line = new Line {'L', comm1, {dest.x, dest.y}};
  Segment *segments[] = {
    (Segment *) arc1,
    (Segment *) line
  };
  const char *name = turn1 == TURN_LEFT ? "NaiveL" : "NaiveR";
  return new_path(name, 2, segments);
}
