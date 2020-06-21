#include <math.h>
#include "paradrone.h"

/**
 * Return the horizontal distance we could cover until landing
 */
double flight_distance_remaining(const double alt) {
  const double timeToGround = alt / PARAMOTOR_DESCENTRATE;
  return PARAMOTOR_GROUNDSPEED * timeToGround;
}

/**
 * Return the angle between two velocity vectors (radians)
 */
static double direction_error(PointV *a, PointV *b) {
  // Dot product
  const double magA = hypot(a->vx, a->vy);
  const double magB = hypot(b->vx, b->vy);
  const double dot = (a->vx * b->vx + a->vy * b->vy) / (magA * magB);
  if (dot >= 1) {
    return 0;
  } else if (dot <= -1) {
    return M_PI;
  } else {
    return acos(dot);
  }
}

/**
 * Plan score. Lower is better.
 */
double plan_score(LandingZone *lz, Path *plan) {
  if (plan) {
    // LZ is at origin
    const double distance = hypot(plan->end.x, plan->end.y);
    // const double angle_error = 100 * direction_error(lz->dest, plan->end);
    return distance;
  } else {
    return 100000;
  }
}
