#ifndef _DTYPES_H
#define _DTYPES_H

#include <stdint.h>
#include "messages.h"

#define TURN_LEFT -1
#define TURN_RIGHT 1

/**
 * Paramotor input controls (left and right toggle)
 * 0 = no deflection
 * 255 = full deflection
 */
struct ParaControls {
  uint8_t left;
  uint8_t right;
};

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
  PointV start;
  PointV end;
  uint8_t segment_count;
  Segment *segments[];
};

class LandingZone {
public:
  LatLngAlt destination;
  double landingDirection; // radians

  /** Ground length of final approach */
  const double finalDistance = 100; // meters

  /** Destination, as origin of coordinate system */
  Point3V dest;

  LandingZone(double lat, double lng, double alt, double landingDir);

  LandingZoneMessage pack();

  /**
   * Convert lat, lng to x, y meters centered at destination
   */
  Point to_point(double lat, double lng);

  /**
   * Convert GeoPointV to 3D point with velocity
   */
  Point3V to_point3V(GeoPointV *point);

  /**
   * Landing pattern: start of final approach
   */
  Point3V start_of_final();

  /**
   * Landing pattern: start of base leg
   */
  Point3V start_of_base(int turn);

  /**
   * Landing pattern: start of downwind leg
   */
  Point3V start_of_downwind(int turn);
};

#endif
