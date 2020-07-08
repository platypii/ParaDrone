#include <math.h>
#include <stdlib.h>
#include "geo.h"

/**
 * Find dubins path
 */
Path *dubins(PointV loc, PointV dest, double r, int turn1, int turn2) {
  // First dubins circle, perpendicular to velocity
  const double velocity = hypot(loc.vx, loc.vy);
  if (velocity == 0) {
    // printf("Zero velocity no tangent\n");
    return NULL;
  }
  Circle c1 = {
    .x = loc.x + turn1 * r * loc.vy / velocity,
    .y = loc.y - turn1 * r * loc.vx / velocity,
    .radius = r
  };
  // Second dubins circle
  const double dest_velocity = hypot(dest.vx, dest.vy);
  if (dest_velocity == 0) {
    // printf("Zero dest velocity no tangent\n");
    return NULL;
  }
  Circle c2 = {
    .x = dest.x + turn2 * r * dest.vy / dest_velocity,
    .y = dest.y - turn2 * r * dest.vx / dest_velocity,
    .radius = r
  };
  // Delta of dubin circles
  const double cx_delta = c2.x - c1.x;
  const double cy_delta = c2.y - c1.y;
  const double c_dist = hypot(cx_delta, cy_delta);
  if (turn1 != turn2 && c_dist < 2 * r) {
    // printf("Intersecting dubins circles\n", c2, dest);
    return NULL;
  }
  // Angle from center to center
  const double center_angle = atan2(cx_delta, cy_delta);
  // Commute
  // If turn1 != turn2, then cross circles
  double turn_delta = 0;
  if (turn1 != turn2) {
    turn_delta = (turn1 - turn2) * r / c_dist;
    turn_delta = fmax(-1, fmin(1, turn_delta));
    turn_delta = asin(turn_delta);
  }
  const double commute_angle = center_angle + turn_delta;
  // const double commute_length = sqrt(c_dist * c_dist - turn_delta * turn_delta);
  if (isnan(commute_angle)) {
    // Happens when c1 intersects c2
    // printf("NaN commute angle\n", commute_angle, asin(turn_delta * r / c_dist), turn_delta * r / c_dist);
  }
  // Last touch of first dubin circle (start of commute home)
  Point comm1 = {
    x: c1.x - turn1 * r * cos(commute_angle),
    y: c1.y + turn1 * r * sin(commute_angle)
  };
  // First touch of second dubin circle (beginning of turn to final)
  Point comm2 = {
    x: c2.x - turn2 * r * cos(commute_angle),
    y: c2.y + turn2 * r * sin(commute_angle)
  };
  // Construct path
  Turn *arc1 = new Turn {'T', {loc.x, loc.y}, comm1, c1, turn1};
  Line *line = new Line {'L', comm1, comm2};
  Turn *arc2 = new Turn {'T', comm2, {dest.x, dest.y}, c2, turn2};
  Segment *segments[] = {
    (Segment *) arc1,
    (Segment *) line,
    (Segment *) arc2
  };
  return new_path("dubins", 3, segments);
}
