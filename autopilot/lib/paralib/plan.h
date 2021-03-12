#ifndef _PLAN_H
#define _PLAN_H

#include <vector>
#include "gtypes.h"
#include "landingzone.h"

#define PARAMOTOR_TURNRADIUS 40
#define PARAMOTOR_GROUNDSPEED 12 // m/s
#define PARAMOTOR_CLIMBRATE -3 // m/s
#define PARAMOTOR_GLIDE (-PARAMOTOR_GROUNDSPEED / PARAMOTOR_CLIMBRATE)

#define ALT_NO_TURNS_BELOW 30 // meters
#define ALT_FLARE 6 // meters

Path *search(Point3V loc3, LandingZone *lz, const double r);
Path *search3(GeoPointV *ll, LandingZone *lz, float toggle_speed, float toggle_balance);

Path *straight(PointV loc);

Path *naive(PointV loc, PointV dest, const double r);

Path *dubins(PointV loc, PointV dest, double r, int turn1, int turn2);

Path *shortest_dubins(PointV loc, PointV dest, double radius);

std::vector<Path*> via_waypoints(Point3V loc, LandingZone *lz, double radius);

// Paraglider
double flight_distance_remaining(const double alt);
GeoPointV *para_predict(GeoPointV *loc, double dt, double turn_speed, double turn_balance);

#endif
