#include <Arduino.h>
#include "paradrone.h"

#define delay_short 100
#define delay_long 1000
#define pwm 255

static int calibrate_motor(Motor *motor, int left_speed, int right_speed);

/**
 * Perform a motor calibration routine.
 */
void calibrate() {
  // Calibrate each motor, forward and back
  const int ticks_left_forward = calibrate_motor(motor_left, pwm, 0);
  const int ticks_left_backward = calibrate_motor(motor_left, -pwm, 0);
  const int ticks_right_forward = calibrate_motor(motor_right, 0, pwm);
  const int ticks_right_backward = calibrate_motor(motor_right, 0, -pwm);

  Serial.printf("%.1fs calibration left %d %d right %d %d\n", millis() * 1e-3, ticks_left_forward, ticks_left_backward, ticks_right_forward, ticks_right_backward);

  // Send bluetooth
  bt_send_calibration(ticks_left_forward, ticks_left_backward, ticks_right_forward, ticks_right_backward);

  const int avg_ticks = (ticks_left_forward + ticks_left_backward + ticks_right_forward + ticks_right_backward) / 4;
  const float ticks_per_second = gear_ratio * encoder_ppr * motor_rpm / 60;
  Serial.printf("%.1fs calibration calculated %f measured %d\n", millis() * 1e-3, ticks_per_second, avg_ticks);
  const float rpm = 60.0 * avg_ticks / (gear_ratio * encoder_ppr);
  Serial.printf("%.1fs calibration rpm %f\n", millis() * 1e-3, rpm);

  set_calibration(avg_ticks);
}

static int calibrate_motor(Motor *motor, int left_speed, int right_speed) {
  // Override flight computer
  last_speed_override = millis() + 10000;

  set_motor_speeds(left_speed, right_speed);
  delay(delay_short);
  const int ticks_start = motor->ticks;
  delay(delay_long);
  const int ticks = motor->ticks - ticks_start;

  // Cool down
  set_motor_speeds(0, 0);
  delay(delay_short);

  return ticks;
}
