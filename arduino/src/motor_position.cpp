#include <math.h>
#include <stdint.h>
#include "heltec.h"
#include "paradrone.h"

static void update_position_estimate();
static short speed(const short delta);

// Use shorts to avoid signed/unsigned overflow

// Current best guess of position
// 0 = no deflection, 255 = full deflection
short motor_position_left = 0;
short motor_position_right = 0;
// TODO: Gaussian estimate

// Target motor position
// 0 = no deflection, 255 = full deflection
short motor_target_left = 0;
short motor_target_right = 0;

// Current motor speed
// -255 = full speed down, 255 = full speed up
short motor_speed_left = 0;
short motor_speed_right = 0;

long last_update = -1;

bool motor_running = false;

// If the current position is not the target position, engage the motors
void motor_loop() {
  update_position_estimate();

  // Calculate target delta and speed to get there
  short new_speed_left = speed(motor_target_left - motor_position_left);
  short new_speed_right = speed(motor_target_right - motor_position_right);
  if (new_speed_left != motor_speed_left || new_speed_right != motor_speed_right) {
    set_controls(motor_speed_left, motor_speed_right);
  }
}

/**
 * Accept input as desired toggle position
 * 0 = no deflection
 * 255 = full deflection
 */
void set_position(uint8_t new_left, uint8_t new_right) {
  motor_target_left = new_left;
  motor_target_right = new_right;
}

static short normalize_position(short position) {
  return position < 0 ? 0 : (position > 255 ? 255 : position);
}

/**
 * Use motor speed to estimate toggle position
 */
static void update_position_estimate() {
  long now = millis();
  long delta = last_update - now;
  last_update = now;
  motor_position_left -= motor_speed_left * delta / 8000;
  motor_position_right -= motor_speed_right * delta / 8000;
  motor_position_left = normalize_position(motor_position_left);
  motor_position_right = normalize_position(motor_position_right);
}

/**
 * Return motor speed for a given position delta
 * @return motor speed in range -255..255
 */
static short speed(const short delta) {
  // Start slowing down when delta < 32
  short speed = delta * 8;
  if (speed < -255) return -255;
  else if (speed > 255) return 255;
  else return speed;
}
