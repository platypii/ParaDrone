#ifndef _PLAN_H
#define _PLAN_H

#include "gtypes.h"

#define ALT_NO_TURNS_BELOW 15 // meters // TODO: 30m
#define ALT_FLARE 7 // meters

Path *search(Point3V loc3, LandingZone *lz, const double r);

Path *straight(PointV loc);

Path *naive(PointV loc, PointV dest, const double r);

#endif
