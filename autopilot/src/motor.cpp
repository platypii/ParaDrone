#include <heltec.h>
#include "paradrone.h"
#include "motor.h"

#define DRIVE_FORWARD 0
#define DRIVE_BACKWARD 1
#define DRIVE_FLOAT 2
#define DRIVE_BRAKE 3

// Drive mode when not changing position.
// DRIVE_BRAKE uses H-bridge to inhibit turning.
#define IDLE_MODE DRIVE_FLOAT

// Sequential PWM channels
static int next_pwm_channel = 0;

static float normalize_position(float position);

/**
 * @param pin_hall_a pin number for hall sensor A
 * @param pin_hall_b pin number for hall sensor B
 * @param pin_pwm1 pin number for motor driver IN1
 * @param pin_pwm2 pin number for motor driver IN2
 * @param hall_isr the isr function to attach to interrupt
 */
Motor::Motor(int pin_hall_a, int pin_hall_b, int pin_pwm1, int pin_pwm2, short *config_direction, void hall_isr())
  : pwm_channel1(next_pwm_channel++)
  , pwm_channel2(next_pwm_channel++)
  , config_direction(config_direction)
{
  // Default state
  position = 10;
  // Setup pins
  ledcAttachPin(pin_pwm1, pwm_channel1);
  ledcAttachPin(pin_pwm2, pwm_channel2);
  ledcSetup(pwm_channel1, 20000, 8);
  ledcSetup(pwm_channel2, 20000, 8);
  pinMode(pin_hall_a, INPUT);
  pinMode(pin_hall_b, INPUT);
  // Listen for rising hall sensor
  attachInterrupt(pin_hall_a, hall_isr, RISING);
}

/**
 * Set motor speed directly.
 * -255 = full speed up, 255 = full speed down
 */
void Motor::set_speed(short new_speed) {
  // TODO: Update position estimate
  speed = new_speed;
  const short hw_speed = *config_direction * speed;
  if (hw_speed < 0) {
    set_speed_hw(DRIVE_FORWARD, -hw_speed);
  } else if (hw_speed > 0) {
    set_speed_hw(DRIVE_BACKWARD, hw_speed);
  } else {
    set_speed_hw(IDLE_MODE, 0);
  }
}

/**
 * Set motor speed PWM and direction.
 * @param drive the direction to drive the motor
 * @param speed speed 0..255
 */
void Motor::set_speed_hw(const int drive, uint8_t speed) {
  if (drive == DRIVE_FORWARD) {
    ledcWrite(pwm_channel1, 0);
    ledcWrite(pwm_channel2, speed);
  } else if (drive == DRIVE_BACKWARD) {
    ledcWrite(pwm_channel1, speed);
    ledcWrite(pwm_channel2, 0);
  } else if (drive == DRIVE_FLOAT) {
    ledcWrite(pwm_channel1, 255);
    ledcWrite(pwm_channel2, 255);
  } else if (drive == DRIVE_BRAKE) {
    ledcWrite(pwm_channel1, 0);
    ledcWrite(pwm_channel2, 0);
  }
}

/**
 * Update motor position estimate
 * @param dt milliseconds since last update
 */
void Motor::update(int dt) {
  if (!dt) return;

  // Position change based on encoder ticks
  const float tick_delta = (ticks - last_ticks) / ticks_per_unit;
  const float tick_speed = tick_delta * 255 / (dt * 1e-3) / units_per_second;
  last_ticks = ticks;

  // Position change based on target speed
  const float speed_delta = units_per_second * speed * dt / 255000;

  // Blended position estimate
  const float use_ticks = 0.8;
  position += tick_delta * use_ticks + speed_delta * (1 - use_ticks);

  // Update position based on direction sensing
  const int speed_threshhold = 16;
  if (speed > 0 && tick_speed - speed > speed_threshhold) {
    Serial.printf("Trying to go down, actually going up %d %f\n", speed, tick_speed);
    // position = 0;
  }
  if (speed < 0 && tick_speed - speed > speed_threshhold) {
    Serial.printf("Trying to go up, actually going down %d %f\n", speed, tick_speed);
    // position = 0;
  }

  position = normalize_position(position);

  // Close enough
  // TODO: Use PID instead
  const float epsilon = 0.8;
  if (fabs(position - target) <= epsilon) {
    position = target;
  }
}

/**
 * Position ranges from 0..255
 */
static float normalize_position(float position) {
  return position < 0 ? 0 : (position > 255 ? 255 : position);
}
