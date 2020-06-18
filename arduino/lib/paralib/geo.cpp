#include <math.h>
#include "geo.h"

const double R = 6371000; // earth radius in meters

/**
 * Compute bearing from one point to another
 * @return bearing in radians
 */
double geo_bearing(double lat1, double lng1, double lat2, double lng2) {
  const double lat1r = to_radians(lat1);
  const double lat2r = to_radians(lat2);
  const double lngDelta = to_radians(lng2 - lng1);

  const double y = sin(lngDelta) * cos(lat2r);
  const double x = cos(lat1r) * sin(lat2r) - sin(lat1r) * cos(lat2r) * cos(lngDelta);
  return atan2(y, x);
}

/**
 * Distance between two points in meters
 */
double geo_distance(double lat1, double lng1, double lat2, double lng2) {
  const double lat1r = to_radians(lat1);
  const double lat2r = to_radians(lat2);
  const double latDelta = to_radians(lat2 - lat1);
  const double lngDelta = to_radians(lng2 - lng1);

  const double sinLat = sin(latDelta * 0.5);
  const double sinLng = sin(lngDelta * 0.5);

  const double a = (sinLat * sinLat) + (cos(lat1r) * cos(lat2r) * sinLng * sinLng);
  const double c = 2 * atan2(sqrt(a), sqrt(1 - a));

  return R * c;
}
