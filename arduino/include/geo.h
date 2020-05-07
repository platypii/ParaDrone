#ifndef _GEO_H
#define _GEO_H

#include <stdint.h>
#include <stdlib.h>

#define TURN_LEFT -1
#define TURN_RIGHT 1

struct LatLngAlt {
  double lat;
  double lng;
  double alt;
};

struct GeoPointV {
  long long int millis;
  double lat;
  double lng;
  double alt;
  double climb;
  double vN;
  double vE;
};

struct Point {
  double x;
  double y;
};

struct PointV {
  double x;
  double y;
  double vx;
  double vy;
};

struct Point3V {
  double x;
  double y;
  double alt;
  double vx;
  double vy;
  double climb;
};

struct Circle {
  double x;
  double y;
  double radius;
};

// Segments
// TODO: Union?
struct Segment {
  char segment_type; // L, T
};

struct Line {
  char segment_type; // L
  Point start;
  Point end;
};

struct Turn {
  char segment_type; // T
  Point start;
  Point end;
  Circle circle;
  int turn;
};

struct Path {
  Point start;
  Point end;
  uint8_t segment_count;
  Segment *segments[];
};

// Functions

// Math
double mod360(double degrees);
double to_degrees(double radians);
double to_radians(double degrees);

// Convert
void bearing2(char *str, double degrees);

double geo_bearing(double lat1, double lng1, double lat2, double lng2);
double geo_distance(double lat1, double lng1, double lat2, double lng2);

Path *naive(struct DubinsParams p, double alt);
Path *dubins_layout(struct DubinsParams p, int turn1, int turn2);

Path *segment_fly(Segment *segment, double distance);
double segment_length(Segment *segment);
Path *line_fly(Line *line, double distance);
double line_length(Line *line);
Path *turn_fly(Turn *turn, double distance);
double turn_length(Turn *turn);

Path *new_path(struct Point start, struct Point end, uint8_t segment_count, Segment *segments[]);
Path *path_fly(Path *path, double distance);
void free_path(Path *path);

double flight_distance_remaining(double alt);

#endif
