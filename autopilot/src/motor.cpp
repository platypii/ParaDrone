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
Motor::Motor(int pin_hall_a, int pin_hall_b, int pin_pwm1, int pin_pwm2, void hall_isr())
  : pwm_channel1(next_pwm_channel++)
  , pwm_channel2(next_pwm_channel++)
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
  // Serial.printf("Set ctrl %d %d\n", left, right);
  // TODO: Update position estimate
  speed = new_speed;
  if (speed < 0) {
    set_speed_hw(DRIVE_FORWARD, -speed);
  } else if (speed > 0) {
    set_speed_hw(DRIVE_BACKWARD, speed);
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
 * If the current position is not the target position, engage the motor
 */
void Motor::update(int dt) {
  position += 0.000125 * speed * dt; // 1/8000
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
