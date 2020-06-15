#include <math.h>
#include <stdlib.h>
#include "geo.h"

/**
 * Fly naively to a waypoint.
 * This path will consist of a turn, plus a straight line to the target.
 * You will probably not arrive at your destination in the DIRECTION you want though.
 */
Path *naive(PointV loc, PointV dest, double r) {
  if (hypot(loc.x - dest.x, loc.y - dest.y) < 2 * r) {
    // printf("Naive planner on top of lz");
    return NULL;
  }
  // bearing from loc to destination
  const double bearing = atan2(dest.x - loc.x, dest.y - loc.y);
  // velocity bearing
  const double yaw = atan2(loc.vx, loc.vy);
  // shorter to turn right or left?
  const double delta = bearing - yaw;
  const int turn1 = delta < 0 ? TURN_LEFT : TURN_RIGHT;
  // Compute path for naive
  const double velocity = hypot(loc.vx, loc.vy);
  Circle c1 = {
    .x = loc.x + turn1 * r * loc.vy / velocity,
    .y = loc.y - turn1 * r * loc.vx / velocity,
    .radius = r
  };
  // Angle from circle center to target
  const double center_angle = atan2(dest.x - c1.x, dest.y - c1.y);
  // Commute
  const double cdest = hypot(c1.x - dest.x, c1.y - dest.y);
  const double offset = turn1 * asin(r / cdest);
  const double commute_angle = center_angle + offset;
  // const double commute_length = sqrt(cdest * cdest - r * r);
  // Last touch of first dubin circle (start of commute home)
  Point comm1 = {
    x: c1.x - turn1 * r * cos(commute_angle),
    y: c1.y + turn1 * r * sin(commute_angle)
  };
  // Construct path
  Turn arc1 = {'T', {loc.x, loc.y}, comm1, c1, turn1};
  Line line = {'L', comm1, {dest.x, dest.y}};
  Segment **segments = new Segment*[2];
  segments[0] = (Segment *) &arc1;
  segments[1] = (Segment *) &line;
  return new_path(2, segments);
}
