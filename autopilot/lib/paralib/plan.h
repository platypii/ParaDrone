#ifndef _PLAN_H
#define _PLAN_H

#include <vector>
#include "gtypes.h"
#include "landingzone.h"

#define ALT_NO_TURNS_BELOW 15 // meters // TODO: 30m
#define ALT_HANDS_UP 12 // meters
#define ALT_FLARE 6 // meters

Path *search(Point3V loc3, LandingZone *lz, const double r);

Path *straight(PointV loc);

Path *naive(PointV loc, PointV dest, const double r);

Path *dubins(PointV loc, PointV dest, double r, int turn1, int turn2);

Path *shortest_dubins(PointV loc, PointV dest, double radius);

std::vector<Path*> via_waypoints(Point3V loc, LandingZone *lz, double radius);

#endif
