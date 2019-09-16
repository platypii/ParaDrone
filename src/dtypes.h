#ifndef _DTYPES_H
#define _DTYPES_H

struct GeoPointV {
  long long int millis;
  double lat;
  double lng;
  double alt;
  double climb;
  double vN;
  double vE;
};

void test();

// Math
double mod360(double degrees);
double to_degrees(double radians);
double to_radians(double degrees);

// GPS
void parse_nmea(char *line);
void update_location(struct GeoPointV point);

// Logger
void log_point(struct GeoPointV point);

#endif
