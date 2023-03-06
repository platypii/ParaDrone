#include <Arduino.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "paradrone.h"

#define parseInt2(a, b) ((a - '0') * 10 + (b - '0'))
#define KNOT 0.514444

static void parse_gga(const char *line);
static void parse_rmc(char *line);
static double parse_degrees_minutes(const char *dm, const char *nsew);
static uint64_t parse_date(const char *str);
static uint64_t parse_time(const char *str);
static double parse_double(const char *str);

// Global altitude
static double altitude = NAN;

void parse_nmea(char *line) {
  if (strnlen(line, 255) < 7) {
    return;
  }
  if (strncmp(line + 3, "RMC,", 4) == 0) {
    parse_rmc(line);
  } else if (strncmp(line + 3, "GGA,", 4) == 0) {
    parse_gga(line);
  } else if (strncmp(line + 3, "TXT,01,01,01,More than 100 frame errors,", 40) == 0) {
    Serial.printf("%.1fs gps frame error\n", millis() * 1e-3);
    error = "GPS Frame Err";
  } else {
    // If we receive unexpected commands like GSV then re-init
    Serial.printf("%.1fs nmea error %s\n", millis() * 1e-3, line);
    ublox_init();
    // Only a problem if it persists
    if (millis() > 5000) {
      error = "GPS Cmd Err";
    }
  }
}

/**
 * Parse GPS altitude from GGA sentence
 */
static void parse_gga(const char *line) {
  // Parse altitude from split[9]
  int commaCount = 0;
  for (int i = 0; line[i]; i++) {
    if (line[i] == ',') {
      if (++commaCount == 9) {
        altitude = parse_double(line + i + 1);
      }
    }
  }
}

/**
 * Parse location from RMC sentence
 */
static void parse_rmc(char *line) {
  // char timeStr[10], status, latDM[11], latNS, lngDM[11], lngEW, dateStr[7];
  const char *TOK = ",";
  strsep(&line, TOK); // command
  char *timeStr = strsep(&line, TOK);
  strsep(&line, TOK); // status
  char *latDM = strsep(&line, TOK);
  char *latNS = strsep(&line, TOK);
  char *lngDM = strsep(&line, TOK);
  char *lngEW = strsep(&line, TOK);
  char *hspeedStr = strsep(&line, TOK);
  char *bearingStr = strsep(&line, TOK);
  char *dateStr = strsep(&line, TOK);

  if (!dateStr || !timeStr) {
    return;
  }

  // sscanf(line, "$GPRMC,%s,%c,%s,%c,%s,%c,%lf,%lf,%s", timeStr, &status, latDM, &latNS, lngDM, &lngEW, &hspeed, &bearing, dateStr);
  const uint64_t millis = parse_date(dateStr) + parse_time(timeStr);
  const double lat = parse_degrees_minutes(latDM, latNS);
  const double lng = parse_degrees_minutes(lngDM, lngEW);
  const double climb = NAN; // TODO: Kalman
  const double hspeed = parse_double(hspeedStr) * KNOT;
  double bearing = parse_double(bearingStr);
  if (isnan(bearing)) {
    // At low speeds, bearing is often NaN
    // Setting it to north is weird but better than throwing away the point
    bearing = 0;
  }
  const double vN = hspeed * cos(to_radians(bearing));
  const double vE = hspeed * sin(to_radians(bearing));

  // Filter out unlikely lat/lng
  if (fabs(lat) > 0.1 || fabs(lng) > 0.1) {
    // Generate point
    GeoPointV *point = new GeoPointV {
      .millis = millis,
      .lat = lat,
      .lng = lng,
      .alt = altitude,
      .vN = vN,
      .vE = vE,
      .climb = climb
    };
    update_location(point);
  }
}

/**
 * Parse DDDMM.MMMM,N into decimal degrees
 * @param dm The latitude or longitude in "DDDMM.MMMM" format
 * @param nsew The modifier "N", "S", "E", or "W"
 * @return The latitude or longitude in decimal degrees
 */
static double parse_degrees_minutes(const char *dm, const char *nsew) {
  if (!*dm || !*nsew) {
    return NAN;
  }
  const char *dot = strchr(dm, '.');
  if (dot) {
    const int index = dot - dm - 2;
    char degrees_str[4] = "";
    strncpy(degrees_str, dm, index);
    const double d = atof(degrees_str);
    const double m = atof(dm + index);
    const double degrees = d + m / 60.0;
    if (*nsew == 'S' || *nsew == 'W') {
      return -degrees;
    } else {
      return degrees;
    }
  } else {
    return NAN;
  }
}

/**
 * Parse DDMMYY into milliseconds since epoch
 */
static uint64_t parse_date(const char *str) {
  struct tm date;
  date.tm_hour = 0;
  date.tm_min = 0;
  date.tm_sec = 0;
  date.tm_isdst = 0;
  date.tm_mday = parseInt2(str[0], str[1]);
  date.tm_mon = parseInt2(str[2], str[3]) - 1;
  date.tm_year = parseInt2(str[4], str[5]) + 100;
  return mktime(&date) * 1000LL;
}

/**
 * Parse HHMMSS.SS UTC time into milliseconds since midnight
 */
static uint64_t parse_time(const char *str) {
  const int h = parseInt2(str[0], str[1]);
  const int m = parseInt2(str[2], str[3]);
  const int s = parseInt2(str[4], str[5]);
  const int hundredths = parseInt2(str[7], str[8]);
  return h * 3600000 + m * 60000 + s * 1000 + hundredths * 10;
}

/**
 * Parse double from string, nan on parse error.
 */
static double parse_double(const char *str) {
  char *end;
  const double parsed = strtod(str, &end);
  if (str != end) {
    return parsed;
  } else {
    return NAN;
  }
}
