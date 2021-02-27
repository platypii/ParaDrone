#include <Arduino.h>
#include "landingzone.h"
#include "messages.h"

LandingZoneMessage pack_lz(LandingZone *lz) {
  if (lz) {
    return LandingZoneMessage {
      'Z',
      (int)(lz->destination.lat * 1e6), // microdegrees
      (int)(lz->destination.lng * 1e6), // microdegrees
      (short)(lz->destination.alt * 10), // decimeters
      (short)(lz->landingDirection * 1000) // milliradians
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

SpeedMessage *pack_speed(GeoPointV *point) {
  return new SpeedMessage {
    'D',
    (int)(point->lat * 1e6), // microdegrees
    (int)(point->lng * 1e6), // microdegrees
    (short)(point->alt * 10), // decimeters
    (short)(point->vN * 0.01), // cm/s
    (short)(point->vE * 0.01), // cm/s
    (short)(point->climb * 0.01) // cm/s
  };
}

GeoPointV *unpack_speed(SpeedMessage *packed) {
  return new GeoPointV {
    millis(),
    packed->lat * 1e-6, // microdegrees
    packed->lng * 1e-6, // microdegrees
    packed->alt * 0.1, // decimeters
    packed->vN * 0.1, //
    packed->vE * 0.1, //
    packed->climb * 0.1, //
  };
}
