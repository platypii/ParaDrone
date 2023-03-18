#include <math.h>
#include <string.h>

/**
 * Print human readable bearing into str
 * @param str string to write to
 * @param degrees bearing in degrees
 */
void bearing2(char *str, double degrees) {
  if (isnan(degrees)) {
    *str = '\0';
  } else {
    degrees = fmod(degrees, 360);
    if (degrees < 0) degrees += 360;
    if (337.5 <= degrees || degrees < 22.5)
      strncpy(str, "N", 2);
    else if (degrees < 67.5)
      strncpy(str, "NE", 3);
    else if (degrees < 112.5)
      strncpy(str, "E", 2);
    else if (degrees < 157.5)
      strncpy(str, "SE", 3);
    else if (degrees < 202.5)
      strncpy(str, "S", 2);
    else if (degrees < 247.5)
      strncpy(str, "SW", 3);
    else if (degrees < 292.5)
      strncpy(str, "W", 2);
    else
      strncpy(str, "NW", 3);
  }
}
