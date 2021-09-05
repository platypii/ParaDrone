#include <Arduino.h>
#include "paradrone.h"
#include "path.h"
#include "plan.h"

// Last RC command received time
long last_rc_millis = -RC_OVERRIDE_MILLIS; // Don't override on reboot
long last_speed_override = -1;

Path *current_plan;
const char *current_plan_name;

const char *land = "Final";
const char *flare = "Flare";

/**
 * Return true if R/C input was made in the last few seconds
 */
bool rc_override() {
  return millis() - last_rc_millis <= RC_OVERRIDE_MILLIS;
}

/**
 * Return true autopilot should be flying
 */
static bool autopilot_enabled() {
  return config_flight_mode == MODE_AUTO && !rc_override();
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
  set_motor_speeds(new_left, new_right);
  last_speed_override = millis();
  // Special case for full up // TODO: Why? What about 1 toggle up?
  if (new_left <= -254 && new_right <= -254) set_toggles(0, 0);
}

/**
 * Called when we receive an R/C toggle position command
 */
void rc_set_position(uint8_t new_left, uint8_t new_right) {
  last_rc_millis = millis();
  set_toggles(new_left, new_right);
}

/**
 * Called when a new location arrives to begin planning
 */
void planner_update_location(GeoPointV *point) {
  // TODO: Also check for rc_override()?
  if (config_landing_zone && autopilot_enabled()) {
    const double alt_agl = point->alt - config_landing_zone->destination.alt;

    if (isnan(alt_agl)) {
      // No alt, do nothing
      current_plan_name = NULL;
    } else if (alt_agl < ALT_FLARE) {
      // Flare!!
      set_toggles(255, 255);
      current_plan_name = flare;
    } else if (alt_agl < ALT_NO_TURNS_BELOW) {
      // Hands up for landing
      set_toggles(0, 0);
      // set_motor_speed(-255, -255); // TODO: Full speed up?
      current_plan_name = land;
    } else if (valid_point(point)) {
      // Compute plan
      Path *new_plan = search3(point, config_landing_zone, get_turn_speed(), get_turn_balance());
      if (current_plan) free_path(current_plan);
      current_plan = new_plan;
      current_plan_name = new_plan->name;
      ParaControls ctrl = path_controls(current_plan);
      set_toggles(ctrl.left, ctrl.right);
      const double error_x = current_plan->end.x;
      const double error_y = current_plan->end.y;
      const double landing_error = sqrt(error_x * error_x + error_y * error_y);
      Serial.printf("%.1fs plan %s len %.1fm error %.1f\n", millis() * 1e-3, current_plan->name, path_length(current_plan), landing_error);
    } else {
      // Do nothing
      current_plan_name = NULL;
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
      set_toggles(10, 10);
    } else if (gps_expired()) {
      // Autopilot + expired GPS signal, put glider into slight right turn
      set_toggles(1, 10);
    }
  }
}
