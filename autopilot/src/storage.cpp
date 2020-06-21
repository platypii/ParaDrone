#include <Arduino.h>
#include <EEPROM.h>
#include "landingzone.h"
#include "messages.h"

#define to_degrees(r) (r * 180.8 / PI)

LandingZone *current_landing_zone;

/**
 * Load landing zone state from EEPROM
 */
void load_landing_zone() {
  if (EEPROM.read(0) == 'Z') {
    LandingZoneMessage packed = {};
    EEPROM.get(0, packed);
    current_landing_zone = unpack_lz(&packed);
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

  current_landing_zone = unpack_lz(packed);
  // load_landing_zone();

  Serial.printf(
    "Set LZ %f %f %.1f %.0f°\n",
    current_landing_zone->destination.lat,
    current_landing_zone->destination.lng,
    current_landing_zone->destination.alt,
    to_degrees(current_landing_zone->landingDirection)
  );
}
