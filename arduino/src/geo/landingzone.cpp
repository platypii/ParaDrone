#include <EEPROM.h>
#include <math.h>
#include <stdio.h>
#include "messages.h"
#include "paradrone.h"

#define to_degrees(r) (r * 180.8 / PI)

LandingZone *current_landing_zone;

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
    .alt = alt,
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
    .alt = loc->alt,
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
    .x = -finalDistance * turn * dest.vy,
    .y = -finalDistance * turn * dest.vx,
    .alt = dest.alt + 3 * finalDistance / PARAMOTOR_GLIDE,
    .vx = -dest.vx,
    .vy = -dest.vy,
    .climb = PARAMOTOR_DESCENTRATE
  };
  return point;
}

LandingZoneMessage LandingZone::pack() {
  LandingZoneMessage msg = {
    'Z',
    (int)(destination.lat * 1e6), // microdegrees
    (int)(destination.lng * 1e6), // microdegrees
    (short)(destination.alt * 10), // decimeters
    (short)(landingDirection * 1000) // milliradians
  };
  return msg;
}

static LandingZone *unpack(LandingZoneMessage *packed) {
  return new LandingZone(
    packed->lat * 1e-6, // microdegrees
    packed->lng * 1e-6, // microdegrees
    packed->alt * 0.1, // decimeters
    packed->landing_direction * 0.001 // milliradians
  );
}

/**
 * Load landing zone state from EEPROM
 */
void load_landing_zone() {
  if (EEPROM.read(0) == 'Z') {
    LandingZoneMessage packed = {};
    EEPROM.get(0, packed);
    current_landing_zone = unpack(&packed);
    Serial.printf(
      "Load LZ %f %f %.1f %.0f°\n",
      current_landing_zone->destination.lat,
      current_landing_zone->destination.lng,
      current_landing_zone->destination.alt,
      to_degrees(current_landing_zone->landingDirection)
    );
  }
}

/**
 * Set landing zone from packed lz message
 */
void set_landing_zone(const char *bytes) {
  LandingZoneMessage *packed = (LandingZoneMessage*) bytes;
  // Persist to EEPROM
  EEPROM.put(0, *packed);
  EEPROM.commit();

  current_landing_zone = unpack(packed);
  // load_landing_zone();

  Serial.printf(
    "Set LZ %f %f %.1f %.0f°\n",
    current_landing_zone->destination.lat,
    current_landing_zone->destination.lng,
    current_landing_zone->destination.alt,
    to_degrees(current_landing_zone->landingDirection)
  );
}
