#include <Arduino.h>
#include "paradrone.h"

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
