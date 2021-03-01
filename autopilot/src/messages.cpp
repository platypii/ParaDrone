#include <Arduino.h>
#include "landingzone.h"
#include "messages.h"

LandingZoneMessage pack_lz(LandingZone *lz) {
  if (lz) {
    return LandingZoneMessage {
      .msg_type = 'Z',
      .lat = (int)(lz->destination.lat * 1e6), // microdegrees
      .lng = (int)(lz->destination.lng * 1e6), // microdegrees
      .alt = (short)(lz->destination.alt * 10), // decimeters
      .landing_direction = (short)(lz->landingDirection * 1000) // milliradians
    };
  } else {
    // No LZ
    return LandingZoneMessage {'Z'};
  }
}

LandingZone *unpack_lz(LandingZoneMessage *packed) {
  return new LandingZone(
    packed->lat * 1e-6, // microdegrees
    packed->lng * 1e-6, // microdegrees
    packed->alt * 0.1, // decimeters
    packed->landing_direction * 0.001 // milliradians
  );
}

SpeedMessage pack_speed(GeoPointV *point) {
  return SpeedMessage {
    .msg_type = 'D',
    .lat = (int)(point->lat * 1e6), // microdegrees
    .lng = (int)(point->lng * 1e6), // microdegrees
    .alt = (short)(point->alt * 10), // decimeters
    .vN = (short)(point->vN * 100), // cm/s
    .vE = (short)(point->vE * 100), // cm/s
    .climb = (short)(point->climb * 100) // cm/s
  };
}

GeoPointV *unpack_speed(SpeedMessage *packed) {
  return new GeoPointV {
    .millis = millis(),
    .lat = packed->lat * 1e-6, // microdegrees
    .lng = packed->lng * 1e-6, // microdegrees
    .alt = packed->alt * 0.1, // decimeters
    .vN = packed->vN * 0.01, // cm/s
    .vE = packed->vE * 0.01, // cm/s
    .climb = packed->climb * 0.01 // cm/s
  };
}
