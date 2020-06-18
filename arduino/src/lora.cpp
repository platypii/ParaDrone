#include "messages.h"
#include "paradrone.h"
#include "heltec.h"

#define MAX_PACKET_SIZE 11

static void lora_read();
static void on_receive(int packet_size);

// LoRa transmission is disabled until we receive a packet
bool lora_enabled = false;
static size_t bytes_ready = 0; // Are bytes ready to be read?
static long last_received_millis = -1;

void lora_init() {
  if (!LoRa.begin(LORA_BAND, true)) {
    Serial.println("LoRa init failed");
  }
  // LoRa.setPreambleLength();
  // LoRa.setSignalBandwidth(125E3); // 250E3, 125E3*, 62.5E3, ...
  // LoRa.setSPIFrequency();
  // LoRa.setSpreadingFactor(9); // 7..12 lower = more chirp/s = faster data, higher = better sensitivity. Default 11
  // LoRa.setTxPower(20, ); // Default 14
  // LoRa.setTxPowerMax(20);
  LoRa.setCodingRate4(8); // 5..8
  LoRa.setSyncWord(0xBA);
  LoRa.enableCrc();
  LoRa.onReceive(on_receive);
  LoRa.receive();
}

void lora_loop() {
  if (bytes_ready) {
    lora_read();
    bytes_ready = 0;
    if (!lora_enabled) {
      Serial.println("LoRa enabled");
      lora_enabled = true;
    }
  }
}

static void lora_send_raw(uint8_t *msg, size_t size) {
  // long start_time = millis();
  LoRa.beginPacket(); // Explicit header mode for variable size packets
  LoRa.write(msg, size);
  LoRa.endPacket();
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

static void lora_send_lz() {
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

static void lora_read() {
  uint8_t packet[MAX_PACKET_SIZE];

  // Read packet
  LoRa.parsePacket();
  for (int i = 0; i < bytes_ready && i < MAX_PACKET_SIZE; i++) {
    packet[i] = (uint8_t) LoRa.read();
  }

  if (packet[0] == 'C' && bytes_ready == 3) {
    // Parse controls
    uint8_t left = packet[1];
    uint8_t right = packet[2];
    rc_set_position(left, right);
    Serial.printf("LoRa ctrl %d, %d\n", left, right);
  } else if (packet[0] == 'P' && bytes_ready == 1) {
    Serial.printf("LoRa ping\n");
  } else if (packet[0] == 'Q' && bytes_ready == 1) {
    // Send LZ in response
    // delay(50); // TODO
    lora_send_lz();
    Serial.printf("LoRa query\n");
  } else if (packet[0] == 'Z' && bytes_ready == 13) {
    set_landing_zone((char*) packet);
    Serial.printf("LoRa set lz\n");
  } else {
    Serial.printf("LoRa %c size %d: ", packet[0], bytes_ready);
    for (int i = 0; i < bytes_ready; i++) {
      Serial.printf("%02x", packet[i]);
    }
    Serial.printf("\n");
  }

  screen_update();
}

static void on_receive(int packet_size) {
  // Don't do any real work here inside ISR
  bytes_ready = packet_size;
  last_received_millis = millis();
}
