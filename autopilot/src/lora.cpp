#include <heltec.h>
#include "messages.h"
#include "paradrone.h"

#define MAX_PACKET_SIZE 20 // Same as BT

static void lora_read(int parse_len);

// LoRa transmission is disabled until we receive a packet
bool lora_enabled = false;
static long last_received_millis = -1;

void lora_init() {
  if (!LoRa.begin(motor_config.frequency, true)) { // TODO: while?
    Serial.println("LoRa init failed");
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
      Serial.println("LoRa enabled");
      lora_enabled = true;
    }
  }
}

static void lora_read(int parse_len) {
  uint8_t buffer[MAX_PACKET_SIZE];
  int buffer_len = 0;
  // Read bytes
  while (LoRa.available() && buffer_len < MAX_PACKET_SIZE) {
    buffer[buffer_len++] = LoRa.read();
  }

  if (parse_len != buffer_len) {
    Serial.printf("LoRa %.1fs len %d %d\n", millis() * 1e-3, parse_len, buffer_len);
  }

  if (buffer[0] == 'M' && buffer_len == 2) {
    Serial.printf("LoRa %.1fs mode %d\n", millis() * 1e-3, buffer[1]);
    set_flight_mode(buffer[1]);
  } else if (buffer[0] == 'P' && buffer_len == 1) {
    Serial.printf("LoRa %.1fs ping\n", millis() * 1e-3);
  } else if (buffer[0] == 'Q' && buffer_len == 2) {
    Serial.printf("LoRa %.1fs Q %c\n", millis() * 1e-3, buffer[1]);
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
    Serial.printf("LoRa %.1fs motor speed %d %d\n", millis() * 1e-3, left, right);
  } else if (buffer[0] == 'T' && buffer_len == 3) {
    // Parse controls
    uint8_t left = buffer[1];
    uint8_t right = buffer[2];
    rc_set_position(left, right);
    Serial.printf("LoRa %.1fs toggle %d %d\n", millis() * 1e-3, left, right);
  } else if (buffer[0] == 'Z' && buffer_len == 13) {
    set_landing_zone((LandingZoneMessage*) buffer);
    Serial.printf("LoRa %.1fs set lz\n", millis() * 1e-3);
  } else {
    Serial.printf("LoRa %.1fs unknown %c size %d: ", millis() * 1e-3, buffer[0], buffer_len);
    for (int i = 0; i < buffer_len; i++) {
      Serial.printf("%02x", buffer[i]);
    }
    Serial.printf("\n");
  }

  LoRa.receive(); // Put it back in receive mode

  screen_update();
}
