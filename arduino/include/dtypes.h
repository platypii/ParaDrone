#ifndef _DTYPES_H
#define _DTYPES_H

struct DConfig {
  int berry_gps;
  int lz_count;
  struct LandingZone *lzs;
};

/**
 * Paramotor input controls (left and right toggle)
 * 0.0 = no deflection
 * 1.0 = full deflection
 */
struct ParaControls {
  double left;
  double right;
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

struct PointV {
  double x;
  double y;
  double vx;
  double vy;
};

struct LandingZone {
  struct LatLngAlt destination;
  double landingDirection; // radians

  /** Ground length of final approach, in meters */
  double finalDistance;

  /** Destination, as origin of coordinate system */
  struct PointV dest;
};

#endif
