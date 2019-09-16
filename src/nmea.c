#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "dtypes.h"

#define parseInt2(a, b) ((a - '0') * 10 + (b - '0'))

static void parse_gga(const char *line);
static void parse_rmc(char *line);
static double parse_degrees_minutes(const char *dm, char *nsew);
static long long int parse_date(const char *str);
static long long int parse_time(const char *str);

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
        altitude = atof(line + i + 1);
      }
    }
  }
}

/**
 * Parse location from RMC sentence
 */
static void parse_rmc(char *line) {
  // char timeStr[10], status, latDM[11], latNS, lngDM[11], lngEW, dateStr[7];
  char *TOK = ",";
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
  const long long int millis = parse_date(dateStr) + parse_time(timeStr);
  const double lat = parse_degrees_minutes(latDM, latNS);
  const double lng = parse_degrees_minutes(lngDM, lngEW);
  const double climb = NAN; // TODO: Kalman
  const double hspeed = atof(hspeedStr);
  const double bearing = atof(bearingStr);
  const double vN = hspeed * cos(to_radians(bearing));
  const double vE = hspeed * sin(to_radians(bearing));

  // Filter out unlikely lat/lng
  if (fabs(lat) > 0.1 || fabs(lng) > 0.1) {
    // Generate point
    struct GeoPointV point = {millis, lat, lng, altitude, climb, vN, vE};
    update_location(point);
  }
}

/**
 * Parse DDDMM.MMMM,N into decimal degrees
 * @param dm The latitude or longitude in "DDDMM.MMMM" format
 * @param nsew The modifier "N", "S", "E", or "W"
 * @return The latitude or longitude in decimal degrees
 */
static double parse_degrees_minutes(const char *dm, char *nsew) {
  if (!*dm || !*nsew) {
    return NAN;
  }
  int index = strchr(dm, '.') - dm - 2;
  char degrees[4] = "";
  strncpy(degrees, dm, index);
  double d = atof(degrees);
  double m = atof(dm + index);
  if (*nsew == 'S' || *nsew == 'W') {
    d = -d;
  }
  return d + m / 60.0;
}

/**
 * Parse DDMMYY into milliseconds since epoch
 */
static long long int parse_date(const char *str) {
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
static long long int parse_time(const char *str) {
  const int h = parseInt2(str[0], str[1]);
  const int m = parseInt2(str[2], str[3]);
  const int s = parseInt2(str[4], str[5]);
  const int hundredths = parseInt2(str[7], str[8]);
  return h * 3600000 + m * 60000 + s * 1000 + hundredths * 10;
}
