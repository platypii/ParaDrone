#include <Arduino.h>
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
static float expected_ticks_per_sec(int speed);

/**
 * Construct a new motor object and bind to pins.
 * @param pin_hall_a pin number for hall sensor A
 * @param pin_hall_b pin number for hall sensor B
 * @param pin_pwm1 pin number for motor driver IN1
 * @param pin_pwm2 pin number for motor driver IN2
 * @param config_direction the motor direction (-1, 1)
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
  ledcSetup(pwm_channel1, 20000, 8);
  ledcSetup(pwm_channel2, 20000, 8);
  ledcAttachPin(pin_pwm1, pwm_channel1);
  ledcAttachPin(pin_pwm2, pwm_channel2);
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

  const int tick_delta = ticks - last_ticks;
  last_ticks = ticks;

  // Position change based on encoder ticks
  const float tick_distance = tick_delta / ticks_per_unit;
  const float ticks_per_sec = tick_delta / (dt * 1e-3);

  // Position change based on pwm speed
  const float pwm_ticks_per_sec = expected_ticks_per_sec(speed);
  const float pwm_distance = pwm_ticks_per_sec / ticks_per_unit * dt * 1e-3;

  // Blended position estimate
  const float use_ticks = 0.8;
  position += tick_distance * use_ticks + pwm_distance * (1 - use_ticks);

  // Update position based on direction sensing
  const int speed_threshhold = 16;
  if (speed > 0 && ticks_per_sec - pwm_ticks_per_sec > speed_threshhold) {
    // Serial.printf("%.1fs trying to go down, actually going up %d %f\n", millis() * 1e-3, speed, tick_speed);
    // position = 0;
  }
  if (speed < 0 && ticks_per_sec - pwm_ticks_per_sec > speed_threshhold) {
    // Serial.printf("%.1fs trying to go up, actually going down %d %f\n", millis() * 1e-3, speed, tick_speed);
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

/**
 * Return expected ticks/sec for a given motor PWM speed
 *      ___/
 *     /
 */
static float expected_ticks_per_sec(int speed) {
  if (speed < -64) {
    speed += 64;
  } else if (speed > 64) {
    speed -= 64;
  } else {
    return 0;
  }
  return 10.2 * speed;
}
