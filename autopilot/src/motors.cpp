#include <heltec.h>
#include "paradrone.h"
#include "motor.h"

// Motor control pins
#define PIN_LEFT_IN1 23
#define PIN_LEFT_IN2 22
#define PIN_RIGHT_IN1 17
#define PIN_RIGHT_IN2 2

// Hall sensors
#define PIN_LEFT_HALL_A 37
#define PIN_LEFT_HALL_B 36
#define PIN_RIGHT_HALL_A 33
#define PIN_RIGHT_HALL_B 32

int motor_ticks_left = 0;
int motor_ticks_right = 0;

// TODO: Learn direction of motor ticks
int motor_ticks_flip_left = 1; // -1 to reverse direction
int motor_ticks_flip_right = 1;

static void IRAM_ATTR hall_isr_left();
static void IRAM_ATTR hall_isr_right();

// Left and Right motors
Motor motor_left(PIN_LEFT_HALL_A, PIN_LEFT_HALL_B, PIN_LEFT_IN1, PIN_LEFT_IN2, hall_isr_left, &motor_ticks_left);
Motor motor_right(PIN_RIGHT_HALL_A, PIN_RIGHT_HALL_B, PIN_RIGHT_IN1, PIN_RIGHT_IN2, hall_isr_right, &motor_ticks_right);

/**
 * Set motor speeds directly.
 * -255 = full speed up, 255 = full speed down
 */
void set_motor_speed_raw(short left, short right) {
  // Serial.printf("Set ctrl %d %d\n", left, right);
  motor_left.set_speed(left * config_direction_left);
  motor_right.set_speed(right * config_direction_right);
}

static void IRAM_ATTR hall_isr_left() {
  // Read B when A is rising
  const int b = digitalRead(PIN_LEFT_HALL_B);
  if (motor_speed_left > 0 && b) {
    motor_ticks_left += motor_ticks_flip_left;
  } else if (motor_speed_left < 0 && !b) {
    motor_ticks_left -= motor_ticks_flip_left;
  }
}

static void IRAM_ATTR hall_isr_right() {
  // Read B when A is rising
  const int b = digitalRead(PIN_RIGHT_HALL_B);
  if (motor_speed_right > 0 && b) {
    motor_ticks_right += motor_ticks_flip_right;
  } else if (motor_speed_right < 0 && !b) {
    motor_ticks_right -= motor_ticks_flip_right;
  }
}
