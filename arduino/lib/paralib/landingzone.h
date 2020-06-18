#ifndef _LZ_H
#define _LZ_H

#include "gtypes.h"

class LandingZone {
public:
  LatLngAlt destination;
  double landingDirection; // radians

  /** Ground length of final approach */
  const double finalDistance = 100; // meters

  /** Destination, as origin of coordinate system */
  Point3V dest;

  LandingZone(double lat, double lng, double alt, double landingDir);

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
