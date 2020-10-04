#include <Arduino.h>
#include <math.h>
#include <stdint.h>
#include "paradrone.h"

#define RC_SPEED_OVERRIDE_DURATION 3000

static void update_position_estimate();
static short speed(const float delta);
static float normalize_position(float position);

void set_motor_speed_raw(const short left, const short right);
float get_motor_current_left();
float get_motor_current_right();

// Use shorts to avoid signed/unsigned overflow

// Best guess of position
// 0 = no deflection, 255 = full deflection
float motor_position_left = 0;
float motor_position_right = 10;
// TODO: Gaussian estimate

// Target motor position
// 0 = no deflection, 255 = full deflection
short motor_target_left = 0;
short motor_target_right = 10;

// Current motor speed
// -255 = full speed up, 255 = full speed down
float motor_speed_left = 0;
float motor_speed_right = 0;

// Motor current
float motor_current_left = 0;
float motor_current_right = 0;

long last_update = -1;
long last_speed_override = -1;

bool motor_running = false;

/**
 * If the current position is not the target position, engage the motors
 */
void motor_loop() {
  update_position_estimate();

  // Check motor feedback
  motor_current_left = get_motor_current_left();
  motor_current_right = get_motor_current_right();

  // Only update speed if it hasn't been overridden
  if (last_speed_override < 0 || millis() - last_speed_override > RC_SPEED_OVERRIDE_DURATION) {
    // Calculate target delta and speed to get there
    short new_speed_left = speed(motor_target_left - motor_position_left);
    short new_speed_right = speed(motor_target_right - motor_position_right);
    if (new_speed_left != motor_speed_left || new_speed_right != motor_speed_right) {
      motor_speed_left = new_speed_left;
      motor_speed_right = new_speed_right;
      set_motor_speed_raw(motor_speed_left, motor_speed_right);
    }
  }
}

/**
 * Set motor speeds.
 * This function tracks position and then executes the motor speed change.
 * -255 = full speed up, 255 = full speed down
 */
void set_motor_speed(const short left, const short right) {
  update_position_estimate();
  motor_speed_left = left;
  motor_speed_right = right;
  set_motor_speed_raw(left, right);
  last_speed_override = millis();
}

/**
 * Set desired toggle position
 * 0 = no deflection (toggles up)
 * 255 = full deflection (toggles down)
 */
void set_motor_position(uint8_t new_left, uint8_t new_right) {
  if (motor_target_left != new_left || motor_target_right != new_right) {
    screen_update();
  }
  motor_target_left = new_left;
  motor_target_right = new_right;
}

/**
 * Use motor speed to estimate toggle position
 */
static void update_position_estimate() {
  const long now = millis();
  const long delta = now - last_update;
  last_update = now;
  if (motor_speed_left || motor_speed_right) {
    motor_position_left += motor_speed_left * delta / 8000;
    motor_position_right += motor_speed_right * delta / 8000;
    motor_position_left = normalize_position(motor_position_left);
    motor_position_right = normalize_position(motor_position_right);
    screen_update();
  }
}

/**
 * Return motor speed for a given position delta
 * @return motor speed in range -255..255
 */
static short speed(const float delta) {
  // Start slowing down when delta < 23
  short speed = delta * 10;
  // Minimum speed 32
  if (speed < 0) speed -= 32;
  if (speed > 0) speed += 32;
  // Max speed 255
  if (speed < -255) return -255;
  else if (speed > 255) return 255;
  else return speed;
}

static float normalize_position(float position) {
  return position < 0 ? 0 : (position > 255 ? 255 : position);
}
