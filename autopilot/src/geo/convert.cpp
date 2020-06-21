#include <math.h>
#include <stdio.h>

/**
 * Print human readable bearing into str
 */
void bearing2(char *str, double degrees) {
  if (isnan(degrees)) {
    *str = '\0';
  } else {
    degrees = fmod(degrees, 360);
    if (degrees < 0) degrees += 360;
    if (337.5 <= degrees || degrees < 22.5)
      sprintf(str, "%.0f° N", degrees);
    else if (degrees < 67.5)
      sprintf(str, "%.0f° NE", degrees);
    else if (degrees < 112.5)
      sprintf(str, "%.0f° E", degrees);
    else if (degrees < 157.5)
      sprintf(str, "%.0f° SE", degrees);
    else if (degrees < 202.5)
      sprintf(str, "%.0f° S", degrees);
    else if (degrees < 247.5)
      sprintf(str, "%.0f° SW", degrees);
    else if (degrees < 292.5)
      sprintf(str, "%.0f° W", degrees);
    else
      sprintf(str, "%.0f° NW", degrees);
  }
}
