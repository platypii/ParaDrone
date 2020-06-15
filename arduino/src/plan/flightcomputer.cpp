#include <Arduino.h>
#include "paradrone.h"
#include "plan.h"

// Revert manual control to autopilot after 10 seconds of no RC
#define RC_OVERRIDE_MILLIS 10000

// After 60 seconds of no GPS, revert to slow spiral
#define GPS_EXPIRATION 60000

// last_fix_millis
static long last_rc_millis = -1;
static Path *current_plan;

String current_plan_name = "";

static bool rc_override() {
  return millis() - last_rc_millis <= RC_OVERRIDE_MILLIS;
}

static bool gps_expired() {
  return millis() - last_fix_millis < GPS_EXPIRATION;
}

/**
 * Called when we receive an R/C control command
 */
void rc_set_position(uint8_t new_left, uint8_t new_right) {
  last_rc_millis = millis();
  set_position(new_left, new_right);
}

/**
 * Called when a new location arrives to begin planning
 */
void planner_update_location(GeoPointV *point) {
  if (current_landing_zone && !rc_override()) {
    const double alt_agl = point->alt - current_landing_zone->destination.alt;

    // Time to act, no more planning
    if (alt_agl < ALT_FLARE) {
      set_position(255, 255);
    } else if (alt_agl < ALT_NO_TURNS_BELOW) {
      set_position(0, 0); // TODO: Full speed up
    } else {
      // Compute plan
      Point3V loc3 = current_landing_zone->to_point3V(point);
      current_plan = search(loc3, current_landing_zone);
      ParaControls ctrl = path_controls(current_plan);
      set_position(ctrl.left, ctrl.right);
    }
  }
}

/**
 * Called periodically to ensure that we do something in case of lost signal
 */
void planner_loop() {
  // If its been X seconds since GPS, then put glider into slight right turn
  if (gps_expired()) {
    set_position(0, 10);
  }
}
