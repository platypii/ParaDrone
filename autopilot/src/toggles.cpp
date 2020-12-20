#include "paradrone.h"

/**
 * Sustained rate of speed for current toggle position (m/s)
 */
float get_turn_speed() {
  const float min_speed = 6; // m/s
  const float max_speed = 12; // m/s
  return max_speed - (motor_position_left + motor_position_right) / 512 * (max_speed - min_speed);
}

/**
 * Left/right balance of current toggle position
 */
float get_turn_balance() {
  return (motor_position_right - motor_position_left) / 255; // [-1..1]
}
