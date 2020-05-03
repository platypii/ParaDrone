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

#endif
