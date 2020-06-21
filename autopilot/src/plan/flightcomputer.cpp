#include <Arduino.h>
#include "paradrone.h"
#include "plan.h"

// Revert manual control to autopilot after 10 seconds of no RC
#define RC_OVERRIDE_MILLIS 10000

// After 60 seconds of no GPS, revert to slow spiral
#define GPS_EXPIRATION 60000

// last_fix_millis
static long last_rc_millis = -10000; // Don't wait on reboot when millis = 0
static Path *current_plan;

String current_plan_name = "";

static bool rc_override() {
  return millis() - last_rc_millis <= RC_OVERRIDE_MILLIS;
}

static bool gps_expired() {
  return millis() - last_fix_millis < GPS_EXPIRATION;
}

static bool valid_point(GeoPointV * p) {
  return !isnan(p->alt) && !isnan(p->vN) && !isnan(p->vE);
}

/**
 * Called when we receive an R/C motor speed command
 */
void rc_set_speed(const short new_left, const short new_right) {
  last_rc_millis = millis();
  set_motor_speed(new_left, new_right);
}

/**
 * Called when we receive an R/C toggle position command
 */
void rc_set_position(uint8_t new_left, uint8_t new_right) {
  last_rc_millis = millis();
  set_motor_position(new_left, new_right);
}

/**
 * Called when autopilot updates the plan
 */
void ap_set_position(uint8_t new_left, uint8_t new_right) {
  if (!rc_override()) {
    set_motor_position(new_left, new_right);
  }
}

/**
 * Called when a new location arrives to begin planning
 */
void planner_update_location(GeoPointV *point) {
  if (current_landing_zone && !rc_override()) {
    const double alt_agl = point->alt - current_landing_zone->destination.alt;

    if (isnan(alt_agl)) {
      // No alt, do nothing
    } else if (alt_agl < ALT_FLARE) {
      // Flare!!
      ap_set_position(255, 255);
    } else if (alt_agl < ALT_NO_TURNS_BELOW) {
      // Hands up for landing
      ap_set_position(0, 0); // TODO: Full speed up
    } else if (valid_point(point)) {
      // Compute plan
      Point3V loc3 = current_landing_zone->to_point3V(point);
      Path *new_plan = search(loc3, current_landing_zone, PARAMOTOR_TURNRADIUS);
      if (current_plan) free_path(current_plan);
      current_plan = new_plan;
      ParaControls ctrl = path_controls(current_plan);
      ap_set_position(ctrl.left, ctrl.right);
    } else {
      // Do nothing
    }
  }
}

/**
 * Called periodically to ensure that we do something in case of lost signal
 */
void planner_loop() {
  // If its been X seconds since GPS, then put glider into slight right turn
  if (gps_expired()) {
    ap_set_position(0, 10);
  }
}
