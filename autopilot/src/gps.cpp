#include <Arduino.h>
#include "paradrone.h"

#define GPS_TX 13 // white GPS output
#define GPS_RX 32 // green GPS input

const uint8_t UBLOX_INIT[] PROGMEM = {
  // UBX-CFG-RATE
  // header   class id    len
  // 0xb5, 0x62, 0x06, 0x08, 0x06, 0x00, 0xe8, 0x03, 0x01, 0x00, 0x01, 0x00, 0x01, 0x39, // 1 Hz
  // 0xb5, 0x62, 0x06, 0x08, 0x06, 0x00, 0xc8, 0x00, 0x01, 0x00, 0x01, 0x00, 0xde, 0x6a, // 5 Hz
  // 0xb5, 0x62, 0x06, 0x08, 0x06, 0x00, 0x64, 0x00, 0x01, 0x00, 0x01, 0x00, 0x7a, 0x12, // 10 Hz
  // UBX-CFG-MSG
  // header   class id    len
  // 0xb5, 0x62, 0x06, 0x01, 0x08, 0x00, 0xf0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x24, // GGA off
  0xb5, 0x62, 0x06, 0x01, 0x08, 0x00, 0xf0, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x2b, // GLL off
  0xb5, 0x62, 0x06, 0x01, 0x08, 0x00, 0xf0, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x32, // GSA off
  0xb5, 0x62, 0x06, 0x01, 0x08, 0x00, 0xf0, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x03, 0x39, // GSV off
  // 0xb5, 0x62, 0x06, 0x01, 0x08, 0x00, 0xf0, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x04, 0x40, // RMC off
  0xb5, 0x62, 0x06, 0x01, 0x08, 0x00, 0xf0, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x05, 0x47, // VTG off
  // 0xb5, 0x62, 0x06, 0x01, 0x08, 0x00, 0x01, 0x07, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x18, 0xe1, // NAV-PVT on
};

GeoPointV *last_location;
long last_fix_millis = -1;

static char buffer[256];
static int buffer_index = 0;

void gps_init() {
  // Secondary serial port, since primary interferes with programming
  Serial2.begin(9600, SERIAL_8N1, GPS_TX, GPS_RX);
  Serial2.write(UBLOX_INIT, sizeof(UBLOX_INIT));
}

void gps_loop() {
  // Read lines
  while (Serial2.available())  {
    // Read characters
    while (Serial2.available() && buffer_index < 255) {
      const char ch = Serial2.read();
      buffer[buffer_index] = ch;
      if (ch == '\r' || ch == '\n') {
        if (buffer_index) { // len > 0
          buffer[buffer_index] = '\0';
          // Serial.println(buffer);
          // Parse NMEA sentence
          parse_nmea(buffer);
        }
        buffer_index = 0;
      } else {
        buffer_index++;
      }
    }
  }
}
