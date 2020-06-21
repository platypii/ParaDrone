#ifndef _DTYPES_H
#define _DTYPES_H

#include <stdint.h>
#include "gtypes.h"
#include "messages.h"

/**
 * Paramotor input controls (left and right toggle)
 * 0 = no deflection
 * 255 = full deflection
 */
struct ParaControls {
  uint8_t left;
  uint8_t right;
};

#endif
