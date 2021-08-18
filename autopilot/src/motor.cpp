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

/**
 * @param pin_hall_a pin number for hall sensor A
 * @param pin_hall_b pin number for hall sensor B
 * @param pin_pwm1 pin number for motor driver IN1
 * @param pin_pwm2 pin number for motor driver IN2
 * @param hall_isr the isr function to attach to interrupt
 */
Motor::Motor(int pin_hall_a, int pin_hall_b, int pin_pwm1, int pin_pwm2, void hall_isr(), int *ticks)
  : pwm_channel1(next_pwm_channel++)
  , pwm_channel2(next_pwm_channel++)
{
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
 * Set motor speeds directly.
 * -255 = full speed up, 255 = full speed down
 */
void Motor::set_speed(short speed) {
  // Serial.printf("Set ctrl %d %d\n", left, right);
  if (speed < 0) {
    set_speed_hw(DRIVE_FORWARD, -speed);
  } else if (speed > 0) {
    set_speed_hw(DRIVE_BACKWARD, speed);
  } else {
    set_speed_hw(IDLE_MODE, 0);
  }
}

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
