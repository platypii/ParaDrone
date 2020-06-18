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
 * Plan score. Lower is better.
 */
double plan_score(LandingZone *lz, Path *plan) {
  if (plan) {
    // LZ is at origin
    const double distance = hypot(plan->end.x, plan->end.y);
    return distance;
  } else {
    return 100000;
  }
}
