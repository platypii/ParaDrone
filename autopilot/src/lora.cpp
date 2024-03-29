#include <LoRa.h>
#include "paradrone.h"

#define MAX_PACKET_SIZE 20 // Same as BT

static void lora_read(int parse_len);

// LoRa transmission is disabled until we receive a packet
bool lora_enabled = false;
static long last_received_millis = -1;

void lora_init() {
  // SPI.begin(SCK, MISO, MOSI, SS);
  LoRa.setPins(SS, RST_LoRa, DIO0);
  if (!LoRa.begin(motor_config.frequency)) { // TODO: while?
    Serial.printf("%.1fs lora init failed\n", millis() * 1e-3);
  }
  // LoRa.setPreambleLength();
  // LoRa.setSignalBandwidth(125E3); // 250E3, 125E3*, 62.5E3, ...
  // LoRa.setSPIFrequency();
  LoRa.setSpreadingFactor(10); // 7..12 default 11. lower = more chirp/s = faster data, higher = better sensitivity
  // LoRa.setTxPower(20, RF_PACONFIG_PASELECT_PABOOST); // 5..20 default 14
  // LoRa.setTxPowerMax(20);
  LoRa.setCodingRate4(5); // Lower ECC for downlink
  LoRa.setSyncWord(0xBA);
  LoRa.enableCrc();
  LoRa.receive();
}

void lora_loop() {
  int parse_len = LoRa.parsePacket();
  if (parse_len) {
    last_received_millis = millis();
    lora_read(parse_len);

    if (!lora_enabled) {
      Serial.printf("%.1fs lora enabled\n", millis() * 1e-3);
      lora_enabled = true;
    }
  }
}

/**
 * Parse an incoming LoRa message
 */
static void lora_read(int parse_len) {
  uint8_t buffer[MAX_PACKET_SIZE];
  int buffer_len = 0;
  // Read bytes
  while (LoRa.available() && buffer_len < MAX_PACKET_SIZE) {
    buffer[buffer_len++] = LoRa.read();
  }

  if (parse_len != buffer_len) {
    Serial.printf("%.1fs lora length mismatch %d != %d\n", millis() * 1e-3, parse_len, buffer_len);
  }

  if (buffer[0] == 'M' && buffer_len == 2) {
    // Flight mode
    const uint8_t mode = buffer[1];
    if (mode == MODE_IDLE) {
      Serial.printf("%.1fs lora mode idle\n", millis() * 1e-3);
    } else if (mode == MODE_AUTO) {
      Serial.printf("%.1fs lora mode auto\n", millis() * 1e-3);
    } else {
      Serial.printf("%.1fs lora bad mode %d\n", millis() * 1e-3, mode);
    }
    set_flight_mode(mode);
  } else if (buffer[0] == 'P' && buffer_len == 1) {
    Serial.printf("%.1fs lora ping\n", millis() * 1e-3);
  } else if (buffer[0] == 'Q' && buffer_len == 2) {
    Serial.printf("%.1fs lora query %c\n", millis() * 1e-3, buffer[1]);
    if (buffer[1] == 'Z') {
      // Send LZ in response
      lora_send_lz();
    } else {
      // TODO: send motor config
    }
  } else if (buffer[0] == 'S' && buffer_len == 3) {
    // Message is -127..127, speeds are -255..255
    const short left = ((short)(int8_t) buffer[1]) * 2;
    const short right = ((short)(int8_t) buffer[2]) * 2;
    rc_set_speed(left, right);
    Serial.printf("%.1fs lora motor speed %d %d\n", millis() * 1e-3, left, right);
  } else if (buffer[0] == 'T' && buffer_len == 3) {
    // Toggle position
    uint8_t left = buffer[1];
    uint8_t right = buffer[2];
    rc_set_position(left, right);
    Serial.printf("%.1fs lora toggle %d %d\n", millis() * 1e-3, left, right);
  } else if (buffer[0] == 'Z' && buffer_len == 13) {
    set_landing_zone((LandingZoneMessage*) buffer);
    Serial.printf("%.1fs lora set lz\n", millis() * 1e-3);
  } else {
    Serial.printf("%.1fs lora unexpected %02x size %d: ", millis() * 1e-3, buffer[0], buffer_len);
    for (int i = 0; i < buffer_len; i++) {
      Serial.printf("%02x", buffer[i]);
    }
    Serial.print('\n');
  }

  LoRa.receive(); // Put it back in receive mode

  screen_update();
}

/**
 * Set the LoRa frequency
 */
void lora_set_frequency(long frequency) {
  LoRa.setFrequency(frequency);
}

/**
 * Broadcast a byte array over LoRa
 */
static void lora_send_raw(uint8_t *msg, size_t size) {
  // long start_time = millis();
  LoRa.beginPacket(); // Explicit header mode for variable size packets
  LoRa.write(msg, size);
  LoRa.endPacket();
  LoRa.receive(); // Put it back in receive mode
  // Serial.printf("%.1fs lora sent %d bytes in %ld ms\n", millis() * 1e-3, size, millis() - start_time);
}

/**
 * Broadcast location over LoRa
 */
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

/**
 * Broadcast landing zone over LoRa
 */
void lora_send_lz() {
  if (config_landing_zone) {
    LandingZoneMessage msg = pack_lz(config_landing_zone);
    uint8_t *data = (uint8_t*) &msg;
    size_t len = sizeof(msg);
    lora_send_raw(data, len);
  } else {
    lora_send_raw((uint8_t*) "Z", 1);
  }
}
