#include "paradrone.h"

/**
 * Set desired toggle position
 * 0 = no deflection (toggles up)
 * 255 = full deflection (toggles down)
 */
void set_toggles(uint8_t new_left, uint8_t new_right) {
  if (motor_left.target != new_left || motor_right.target != new_right) {
    screen_update();
  }
  motor_left.target = new_left;
  motor_right.target = new_right;
  // motors updated on next motor_loop()
}

/**
 * Sustained rate of speed for current toggle position (m/s)
 */
float get_turn_speed() {
  const float min_speed = 6; // m/s
  const float max_speed = 12; // m/s
  return max_speed - (motor_left.position + motor_right.position) / 512 * (max_speed - min_speed);
}

/**
 * Left/right balance of current toggle position
 */
float get_turn_balance() {
  return (motor_right.position - motor_left.position) / 255; // -1..1
}
