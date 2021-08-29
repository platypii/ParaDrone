#include <Arduino.h>
#include "paradrone.h"

#define RC_SPEED_OVERRIDE_DURATION 3000

static short speed(const float delta);

long last_update = -1;

/**
 * If the current position is not the target position, engage the motors
 */
void motor_loop() {
  // Update motor position estimates
  const long now = millis();
  const long dt = now - last_update;
  last_update = now;
  motor_left.update(dt);
  motor_right.update(dt);

  // Only update speed if it hasn't been overridden
  if (last_speed_override < 0 || millis() - last_speed_override > RC_SPEED_OVERRIDE_DURATION) {
    // Calculate target delta and speed to get there
    const short new_speed_left = speed(motor_left.target - motor_left.position);
    const short new_speed_right = speed(motor_right.target - motor_right.position);
    set_motor_speeds(new_speed_left, new_speed_right);
  }

  if (motor_left.speed || motor_right.speed) {
    screen_update();
  }
}

/**
 * Set desired toggle position
 * 0 = no deflection (toggles up)
 * 255 = full deflection (toggles down)
 */
void set_motor_target(uint8_t new_left, uint8_t new_right) {
  if (motor_left.target != new_left || motor_right.target != new_right) {
    screen_update();
  }
  motor_left.target = new_left;
  motor_right.target = new_right;
  // TODO: update motors speeds?
}

/**
 * Return motor speed for a given position delta
 * @return motor speed in range -255..255
 */
static short speed(const float delta) {
  // Start slowing down when delta < 23
  short speed = delta * 10;
  // Minimum speed 40
  if (speed < 0) speed -= 40;
  if (speed > 0) speed += 40;
  // Max speed 255
  if (speed < -255) return -255;
  else if (speed > 255) return 255;
  else return speed;
}
