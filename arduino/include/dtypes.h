#ifndef _DTYPES_H
#define _DTYPES_H

#include "geo.h"

/**
 * Paramotor input controls (left and right toggle)
 * 0.0 = no deflection
 * 1.0 = full deflection
 */
struct ParaControls {
  double left;
  double right;
};

class LandingZone {
public:
  LatLngAlt destination;
  double landingDirection; // radians

  /** Ground length of final approach */
  const double finalDistance = 100; // meters

  /** Destination, as origin of coordinate system */
  PointV dest;

  LandingZone(double lat, double lng, double alt, double landingDir);

  /**
   * Convert lat, lng to x, y meters centered at current location
   */
  Point to_point(double lat, double lng);

  /**
   * Landing pattern: start of final approach
   */
  PointV start_of_final();

  /**
   * Landing pattern: start of base leg
   */
  PointV start_of_base(int turn);
};

#endif
