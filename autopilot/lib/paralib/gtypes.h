#ifndef _GTYPES_H
#define _GTYPES_H

#include <stdint.h>

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

  operator Point() {
    return Point {x, y};
  }

  operator PointV() {
    return PointV {x, y, vx, vy};
  }
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
  const char *name;
  PointV start;
  PointV end;
  uint8_t segment_count;
  Segment *segments[];
};

/**
 * Paramotor input controls (left and right toggle)
 * 0 = no deflection
 * 255 = full deflection
 */
struct ParaControls {
  uint8_t left;
  uint8_t right;
};

#endif
