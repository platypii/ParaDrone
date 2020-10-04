#include <heltec.h>

#define PIN_BATT 37 // 13

// Moving average
static float battery_level = NAN;

static float instant_battery_level();

/**
 * Get battery level as a % of lifetime remaining
 * @return battery level from 0 to 1
 */
float get_battery_level() {
  const float batt = instant_battery_level();
  if (isnan(battery_level)) {
    battery_level = batt;
  } else {
    // Moving average, alpha = 0.1
    battery_level += (batt - battery_level) * 0.1;
  }
  return battery_level;
}

static float instant_battery_level() {
  const float analog = analogRead(PIN_BATT);
  // Observed levels 1085..1375
  // Quadratic curve fit
  const float batt = -43 + 0.0639 * analog - 2.32e-5 * analog * analog;
  if (batt < 0) return 0;
  else if (batt > 1) return 1;
  else return batt;
}
