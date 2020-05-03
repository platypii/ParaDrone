#include <EEPROM.h>
#include <math.h>
#include <stdio.h>
#include "dtypes.h"

class LandingZone {
public:
  LatLngAlt destination;
  double landingDirection; // radians

  /** Ground length of final approach */
  const double finalDistance = 100; // meters

  /** Destination, as origin of coordinate system */
  PointV dest;

  LandingZone(double lat, double lng, double alt, double landingDir) {
    destination = {
      .lat = lat,
      .lng = lng,
      .alt = alt
    };
    landingDirection = landingDir;
    dest = {
      .x = 0,
      .y = 0,
      .vx = cos(landingDirection),
      .vy = sin(landingDirection)
    };
  }

  /**
   * Convert lat, lng to x, y meters centered at current location
   */
  Point to_point(double lat, double lng) {
    const double bearing = geo_bearing(destination.lat, destination.lng, lat, lng);
    const double distance = geo_distance(destination.lat, destination.lng, lat, lng);
    Point point = {
      .x = distance * sin(bearing),
      .y = distance * cos(bearing)
    };
    return point;
  }

  /**
   * Landing pattern: start of final approach
   */
  PointV start_of_final() {
    PointV point = {
      .x = -finalDistance * dest.vx,
      .y = -finalDistance * dest.vy,
      .vx = dest.vx,
      .vy = dest.vy
    };
    return point;
  }

  /**
   * Landing pattern: start of base leg
   */
  PointV start_of_base(int turn) {
    struct PointV point = {
      .x = -finalDistance * (dest.vx - turn * dest.vy),
      .y = -finalDistance * (turn * dest.vx + dest.vy),
      .vx = -turn * dest.vy,
      .vy = turn * dest.vx
    };
    return point;
  }
};

static LandingZone *currentLandingZone;

#pragma pack(1)
struct PackedLZ {
  char msg_type; // 'Z'
  int lat;
  int lng;
  short alt;
  short landingDirection;
};

static LandingZone *unpack(PackedLZ *packed) {
  return new LandingZone(
    packed->lat * 1e-6,
    packed->lng * 1e-6,
    packed->alt * 0.1,
    packed->landingDirection * 0.001
  );
}

/**
 * Load landing zone state from EEPROM
 */
void load_landing_zone() {
  if (EEPROM.read(0) == 'Z') {
    PackedLZ packed = {};
    EEPROM.get(0, packed);
    currentLandingZone = unpack(&packed);
  }
}

/**
 * Set landing zone from packed lz message
 */
void set_landing_zone(const char *bytes) {
  PackedLZ *packed = (PackedLZ*) bytes;
  // Persist to EEPROM
  EEPROM.put(0, *packed);
  currentLandingZone = unpack(packed);
  Serial.println("Set LZ");
  Serial.println(bytes);
  Serial.printf("Set LZ %f %f %f %f\n", currentLandingZone->destination.lat, currentLandingZone->destination.lng, currentLandingZone->destination.alt, currentLandingZone->landingDirection);
}
