#include <math.h>

/**
 * Normalize an angle in degrees into the normal [-180,180) range of longitude
 */
double mod360(double degrees) {
  return fmod((fmod(degrees, 360) + 540.0), 360.0) - 180.0;
}

/**
 * Convert radians to degrees
 */
double to_degrees(double radians) {
  return radians * 180 / M_PI;
}

/**
 * Convert degrees to radians
 */
double to_radians(double degrees) {
  return degrees * M_PI / 180.0;
}
