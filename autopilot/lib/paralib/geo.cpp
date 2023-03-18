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
 * Distance between two points
 * @return distance in meters
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

/**
 * Moves a location along a bearing by a given distance
 * @param lat_degrees starting latitude in degrees
 * @param lng_degrees starting longitude in degrees
 * @param bear bearing in radians
 * @param dist distance in meters
 * @return LatLng ending coordinate
 */
LatLng geo_move_bearing(double lat_degrees, double lng_degrees, double bear, double dist) {
  if (dist == 0) {
    // Fast case for dist = 0
    return {};
  }

  const double d = dist / R;

  const double lat = to_radians(lat_degrees);
  const double lng = to_radians(lng_degrees);

  // Precompute trig
  const double sin_d = sin(d);
  const double cos_d = cos(d);
  const double sin_lat = sin(lat);
  const double cos_lat = cos(lat);
  const double sin_d_cos_lat = sin_d * cos_lat;

  const double lat2 = asin(sin_lat * cos_d + sin_d_cos_lat * cos(bear));
  const double lng2 = lng + atan2(sin(bear) * sin_d_cos_lat, cos_d - sin_lat * sin(lat2));

  const double lat3 = to_degrees(lat2);
  const double lng3 = mod360(to_degrees(lng2));

  return {
    .lat = lat3,
    .lng = lng3
  };
}
