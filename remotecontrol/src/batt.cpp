#include <heltec.h>

#define PIN_BATT 37 // 13

// TODO: Moving average

/**
 * Get battery level as a % of lifetime remaining
 * @return battery level from 0 to 1
 */
float get_battery_level() {
  const float analog = analogRead(PIN_BATT);
  // Observed levels 1085..1375
  // Quadratic curve fit
  const float batt = -43 + 0.0639 * analog - 2.32e-5 * analog * analog;
  if (batt < 0) return 0;
  else if (batt > 1) return 1;
  else return batt;
}
