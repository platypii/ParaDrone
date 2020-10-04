#include <math.h>
#include <stdio.h>
#include "geo.h"
#include "landingzone.h"
#include "plan.h"

LandingZone::LandingZone(double lat, double lng, double alt, double landingDir) {
  destination = {
    .lat = lat,
    .lng = lng,
    .alt = alt
  };
  landingDirection = landingDir;
  dest = {
    .x = 0,
    .y = 0,
    .alt = 0,
    .vx = sin(landingDirection),
    .vy = cos(landingDirection),
    .climb = 0
  };
}

/**
 * Convert lat, lng to x, y meters centered at current location
 */
Point LandingZone::to_point(double lat, double lng) {
  const double bearing = geo_bearing(destination.lat, destination.lng, lat, lng);
  const double distance = geo_distance(destination.lat, destination.lng, lat, lng);
  Point point = {
    .x = distance * sin(bearing),
    .y = distance * cos(bearing)
  };
  return point;
}

/**
 * Convert lat, lng to x, y meters centered at current location
 */
Point3V LandingZone::to_point3V(GeoPointV *loc) {
  const double bearing = geo_bearing(destination.lat, destination.lng, loc->lat, loc->lng);
  const double distance = geo_distance(destination.lat, destination.lng, loc->lat, loc->lng);
  Point3V point = {
    .x = distance * sin(bearing),
    .y = distance * cos(bearing),
    .alt = loc->alt - destination.alt,
    .vx = loc->vE,
    .vy = loc->vN,
    .climb = loc->climb
  };
  return point;
}

/**
 * Landing pattern: start of final approach
 */
Point3V LandingZone::start_of_final() {
  Point3V point = {
    .x = -finalDistance * dest.vx,
    .y = -finalDistance * dest.vy,
    .alt = dest.alt + finalDistance / PARAMOTOR_GLIDE,
    .vx = dest.vx,
    .vy = dest.vy,
    .climb = PARAMOTOR_DESCENTRATE
  };
  return point;
}

/**
 * Landing pattern: start of base leg
 */
Point3V LandingZone::start_of_base(int turn) {
  Point3V point = {
    .x = -finalDistance * (dest.vx - turn * dest.vy),
    .y = -finalDistance * (turn * dest.vx + dest.vy),
    .alt = dest.alt + 2 * finalDistance / PARAMOTOR_GLIDE,
    .vx = -dest.vx,
    .vy = -dest.vy,
    .climb = PARAMOTOR_DESCENTRATE
  };
  return point;
}

/**
 * Landing pattern: start of base leg
 */
Point3V LandingZone::start_of_downwind(int turn) {
  Point3V point = {
    .x = finalDistance * turn * dest.vy,
    .y = -finalDistance * turn * dest.vx,
    .alt = dest.alt + 3 * finalDistance / PARAMOTOR_GLIDE,
    .vx = -dest.vx,
    .vy = -dest.vy,
    .climb = PARAMOTOR_DESCENTRATE
  };
  return point;
}
