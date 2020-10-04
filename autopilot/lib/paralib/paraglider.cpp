#include "plan.h"

/**
 * Return the horizontal distance we could cover until landing
 */
double flight_distance_remaining(const double alt) {
  const double timeToGround = alt / PARAMOTOR_DESCENTRATE;
  return PARAMOTOR_GROUNDSPEED * timeToGround;
}
