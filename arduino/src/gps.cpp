#include <Arduino.h>
#include "paradrone.h"

#define GPS_TX 12 // white GPS output
#define GPS_RX 36 // green GPS input

GeoPointV *last_location;

void init_gps() {
  // Secondary serial port, since primary interferes with programming
  Serial2.begin(9600, SERIAL_8N1, GPS_TX, GPS_RX);
}

void read_gps() {
  // Serial.println("Reading from GPS...");
  char line[256]; // Null terminated NMEA string

  // Read lines
  while (Serial2.available())  {
    // Read characters
    int i = 0;
    while (Serial2.available() && i < 255) {
      line[i] = Serial2.read();
      if (line[i] == '\r' || line[i] == '\n') {
        break;
      }
      i++;
    }
    line[i] = '\0';
    if (i) {
      // Parse NMEA sentence
      parse_nmea(line);
    }
  }
}
