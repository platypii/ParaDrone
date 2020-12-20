#include <math.h>
#include "geo.h"
#include "plan.h"

/**
 * Return the horizontal distance we could cover until landing
 */
double flight_distance_remaining(const double alt) {
  const double timeToGround = -alt / PARAMOTOR_CLIMBRATE;
  return PARAMOTOR_GROUNDSPEED * timeToGround;
}

/**
 * Predict where the glider will be in dt seconds.
 * Takes into account position, speed, and toggle position.
 * TODO: Adjust velocities on WSE
 * TODO: Model wind
 */
GeoPointV *para_predict(GeoPointV *loc, double dt, double turn_speed, double turn_balance) {
  const double alpha = 0.5; // moving average filter applied to toggle inputs to simulate the fact that speed and direction don't change instantly

  double ground_speed = sqrt(loc->vE * loc->vE + loc->vN * loc->vN);

  // Update glider turn (yaw) rate and speed based on toggle position
  // TODO: Special case for straight? Faster?
  ground_speed += (turn_speed - ground_speed) * alpha;
  const double distance = ground_speed * dt;
  // Air bearing
  const double start_bearing = atan2(loc->vE, loc->vN);
  const double end_bearing = start_bearing + distance * turn_balance / PARAMOTOR_TURNRADIUS;
  // The proof of this is beautiful:
  const double chord_bearing = start_bearing + distance * turn_balance / PARAMOTOR_TURNRADIUS / 2;

  // Move lat,lng by distance and bearing of flight path
  LatLng prewind = geo_move_bearing(loc->lat, loc->lng, chord_bearing, distance);

  // Adjust velocity
  const double vE = ground_speed * sin(end_bearing);
  const double vN = ground_speed * cos(end_bearing);

  // Adjust altitude
  const double alt = loc->alt + loc->climb * dt;
  const double climb = loc->climb + (PARAMOTOR_CLIMBRATE - loc->climb) * alpha;

  return new GeoPointV {
    .millis = loc->millis + (long long)(1000 * dt),
    .lat = prewind.lat,
    .lng = prewind.lng,
    .alt = alt,
    .climb = climb,
    .vN = vN,
    .vE = vE
  };
}
