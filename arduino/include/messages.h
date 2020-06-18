#ifndef _MESSAGES_H
#define _MESSAGES_H

#include "landingzone.h"

/**
 * Message with just location.
 * sizeof 11
 */
#pragma pack(1)
struct LocationMessage {
  char msg_type; // 'L'
  int lat; // microdegrees
  int lng; // microdegrees
  short alt; // decimeters
};

/**
 * Message with location and speed.
 * sizeof 17
 */
#pragma pack(1)
struct SpeedMessage {
  char msg_type; // 'S'
  int lat; // microdegrees
  int lng; // microdegrees
  short alt; // decimeters
  short vN; // cm/s
  short vE; // cm/s
  short climb; // cm/s
};

/**
 * Message with landing zone.
 * sizeof 13
 */
#pragma pack(1)
struct LandingZoneMessage {
  char msg_type; // 'Z'
  int lat; // microdegrees
  int lng; // microdegrees
  short alt; // decimeters
  short landing_direction; // milliradians
};

LandingZoneMessage pack_lz(LandingZone *lz);
LandingZone *unpack_lz(LandingZoneMessage *lz);

#endif
