#ifndef _GEO_H
#define _GEO_H

#include "gtypes.h"

// Math
double mod360(double degrees);
double to_degrees(double radians);
double to_radians(double degrees);

// Convert
void bearing2(char *str, double degrees);

double geo_bearing(double lat1, double lng1, double lat2, double lng2);
double geo_distance(double lat1, double lng1, double lat2, double lng2);
LatLng geo_move_bearing(double lat_degrees, double lng_degrees, double bear, double dist);

#endif
