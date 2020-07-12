#include <heltec.h>
#include "messages.h"
#include "paradrone.h"

static void lora_send_raw(uint8_t *msg, size_t size) {
  // long start_time = millis();
  LoRa.beginPacket(); // Explicit header mode for variable size packets
  LoRa.write(msg, size);
  LoRa.endPacket();
  LoRa.receive(); // Put it back in receive mode
  // Serial.printf("LoRa sent %d bytes in %ld ms\n", size, millis() - start_time);
}

void lora_send_location(GeoPointV *point) {
  if (lora_enabled) {
    // Pack point into location message
    LocationMessage msg = {
      'L',
      (int)(point->lat * 1e6), // microdegrees
      (int)(point->lng * 1e6), // microdegrees
      (short)(point->alt * 10) // decimeters
      // (short)(point->vN * 0.01), // cm/s
      // (short)(point->vE * 0.01), // cm/s
      // (short)(point->climb * 0.01) // cm/s
    };
    uint8_t *data = (uint8_t*) &msg;
    size_t len = sizeof(msg);
    lora_send_raw(data, len);
  }
}

void lora_send_lz() {
  if (current_landing_zone != NULL) {
    LandingZoneMessage msg = {
      'Z',
      (int)(current_landing_zone->destination.lat * 1e6), // microdegrees
      (int)(current_landing_zone->destination.lng * 1e6), // microdegrees
      (short)(current_landing_zone->destination.alt * 10), // decimeters
      (short)(current_landing_zone->landingDirection * 1000) // milliradians
    };
    uint8_t *data = (uint8_t*) &msg;
    size_t len = sizeof(msg);
    lora_send_raw(data, len);
  } else {
    // No LZ
    uint8_t data = 'N';
    lora_send_raw(&data, 1);
  }
}
