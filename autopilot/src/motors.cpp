#include <Arduino.h>
#include "paradrone.h"

// Motor control pins
#define PIN_LEFT_IN1 34
#define PIN_LEFT_IN2 36
#define PIN_RIGHT_IN1 20
#define PIN_RIGHT_IN2 26

// Hall sensors
#define PIN_LEFT_HALL_A 46
#define PIN_LEFT_HALL_B 37
#define PIN_RIGHT_HALL_A 38
#define PIN_RIGHT_HALL_B 39

// TODO: Learn direction of motor ticks
static int motor_ticks_flip_left = 1; // -1 to reverse direction
static int motor_ticks_flip_right = 1;

static long last_update = -1;

static void IRAM_ATTR hall_isr_left();
static void IRAM_ATTR hall_isr_right();

// Left and Right motors
Motor *motor_left;
Motor *motor_right;

static short speed(const float delta);

void motor_init() {
  motor_left = new Motor(PIN_LEFT_HALL_A, PIN_LEFT_HALL_B, PIN_LEFT_IN1, PIN_LEFT_IN2, &config_direction_left, hall_isr_left);
  motor_right = new Motor(PIN_RIGHT_HALL_A, PIN_RIGHT_HALL_B, PIN_RIGHT_IN1, PIN_RIGHT_IN2, &config_direction_right, hall_isr_right);
}

/**
 * If the current position is not the target position, engage the motors
 */
void motor_loop() {
  // Update motor position estimates
  const long now = millis();
  const long dt = now - last_update;
  last_update = now;
  motor_left->update(dt);
  motor_right->update(dt);

  // Only update speed if it hasn't been overridden
  if (last_speed_override < 0 || millis() > last_speed_override + RC_SPEED_OVERRIDE_DURATION) {
    // Calculate target delta and speed to get there
    const short new_speed_left = speed(motor_left->target - motor_left->position);
    const short new_speed_right = speed(motor_right->target - motor_right->position);
    set_motor_speeds(new_speed_left, new_speed_right);
  }

  if (motor_left->speed || motor_right->speed) {
    screen_update();
  }
}

/**
 * Set motor speeds directly.
 * -255 = full speed up, 255 = full speed down
 */
void set_motor_speeds(short left, short right) {
  // Serial.printf("%.1fs set speeds %d %d\n", millis() * 1e-3, left, right);
  motor_left->set_speed(left);
  motor_right->set_speed(right);
}

static void IRAM_ATTR hall_isr_left() {
  // Read B when A is rising
  const int b = digitalRead(PIN_LEFT_HALL_B);
  if (motor_left->speed * config_direction_left >= 0 && b) {
    motor_left->ticks += motor_ticks_flip_left;
  } else if (motor_left->speed * config_direction_left <= 0 && !b) {
    motor_left->ticks -= motor_ticks_flip_left;
  }
}

static void IRAM_ATTR hall_isr_right() {
  // Read B when A is rising
  const int b = digitalRead(PIN_RIGHT_HALL_B);
  if (motor_right->speed * config_direction_right >= 0 && b) {
    motor_right->ticks += motor_ticks_flip_right;
  } else if (motor_right->speed * config_direction_right <= 0 && !b) {
    motor_right->ticks -= motor_ticks_flip_right;
  }
}

/**
 * Return motor speed for a given position delta
 * @return motor speed in range -255..255
 */
static short speed(const float delta) {
  // Start slowing down when delta < 20
  short speed = delta * 10;
  // Minimum speed 60
  if (speed < 0) speed -= 60;
  if (speed > 0) speed += 60;
  // Max speed 255
  if (speed < -255) return -255;
  else if (speed > 255) return 255;
  else return speed;
}
