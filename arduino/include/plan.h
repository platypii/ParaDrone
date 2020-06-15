#ifndef _PLAN_H
#define _PLAN_H

#include "dtypes.h"

#define ALT_NO_TURNS_BELOW 30 // meters
#define ALT_FLARE 7 // meters

Path *search(Point3V loc3, LandingZone *lz);

Path *straight(PointV loc);

Path *naive(PointV loc, PointV dest, double r);

#endif
