#include <Arduino.h>
#include "paradrone.h"

#define GPS_TX 12 // white GPS output
#define GPS_RX 36 // green GPS input

GeoPointV *last_location;
long last_fix_millis = -1;

static char buffer[256];
static int buffer_index = 0;

void gps_init() {
  // Secondary serial port, since primary interferes with programming
  Serial2.begin(9600, SERIAL_8N1, GPS_TX, GPS_RX);
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
