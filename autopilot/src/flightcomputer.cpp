#include <Arduino.h>
#include "paradrone.h"
#include "path.h"
#include "plan.h"

// Revert manual control to autopilot after 10 seconds of no RC
#define RC_OVERRIDE_MILLIS 10000

// After 60 seconds of no GPS, revert to slow spiral
#define GPS_EXPIRATION 60000

// last_fix_millis
static long last_rc_millis = -10000; // Don't wait on reboot when millis = 0
static Path *current_plan;

String current_plan_name = "";

/**
 * Return true if R/C input was made in the last few seconds
 */
static bool rc_override() {
  return millis() - last_rc_millis <= RC_OVERRIDE_MILLIS;
}

/**
 * Return true autopilot should be flying
 */
static bool autopilot_enabled() {
  return config_flight_mode == MODE_AP && !rc_override();
}

/**
 * Return true if GPS fix is stale
 */
static bool gps_expired() {
  return millis() - last_fix_millis > GPS_EXPIRATION;
}

/**
 * Return true if GPS point is reasonable
 */
static bool valid_point(GeoPointV * p) {
  return !isnan(p->alt) && !isnan(p->vN) && !isnan(p->vE);
}

/**
 * Called when we receive an R/C motor speed command
 */
void rc_set_speed(const short new_left, const short new_right) {
  last_rc_millis = millis();
  set_motor_speed(new_left, new_right);
  // Special case for full up // TODO: Why? What about 1 toggle up?
  if (new_left <= -254 && new_right <= -254) set_motor_position(0, 0);
}

/**
 * Called when we receive an R/C toggle position command
 */
void rc_set_position(uint8_t new_left, uint8_t new_right) {
  last_rc_millis = millis();
  set_motor_position(new_left, new_right);
}

/**
 * Called when a new location arrives to begin planning
 */
void planner_update_location(GeoPointV *point) {
  if (config_landing_zone && autopilot_enabled()) {
    const double alt_agl = point->alt - config_landing_zone->destination.alt;

    if (isnan(alt_agl)) {
      // No alt, do nothing
    } else if (alt_agl < ALT_FLARE) {
      // Flare!!
      set_motor_position(255, 255);
    } else if (alt_agl < ALT_NO_TURNS_BELOW) {
      // Hands up for landing
      set_motor_position(0, 0);
      // set_motor_speed(-255, -255); // TODO: Full speed up?
    } else if (valid_point(point)) {
      // Compute plan
      Path *new_plan = search3(point, config_landing_zone, get_turn_speed(), get_turn_balance());
      if (current_plan) free_path(current_plan);
      current_plan = new_plan;
      ParaControls ctrl = path_controls(current_plan);
      set_motor_position(ctrl.left, ctrl.right);
      const double error_x = current_plan->end.x;
      const double error_y = current_plan->end.y;
      const double landing_error = sqrt(error_x * error_x + error_y * error_y);
      Serial.printf("Plan %s length %.1fm error %.2f\n", current_plan->name, path_length(current_plan), landing_error);
    } else {
      // Do nothing
    }
  }
}

/**
 * Called periodically to ensure that we do something in case of lost signal
 */
void planner_loop() {
  if (!rc_override()) {
    if (!autopilot_enabled()) {
      // Idle mode
      set_motor_position(10, 10);
    } else if (gps_expired()) {
      // Autopilot + expired GPS signal, put glider into slight right turn
      set_motor_position(1, 10);
    }
  }
}