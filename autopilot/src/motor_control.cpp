#include <heltec.h>
#include "paradrone.h"

// Right motor
#define PIN_M1_IN1 23
#define PIN_M1_IN2 22
// Left motor
#define PIN_M2_IN1 17
#define PIN_M2_IN2 2

// Hall sensors
#define PIN_M1_HALL_A 37
#define PIN_M1_HALL_B 36
#define PIN_M2_HALL_A 33
#define PIN_M2_HALL_B 32

// ESP32 PWM channels
#define CHANNEL_M1_IN1 0
#define CHANNEL_M1_IN2 1
#define CHANNEL_M2_IN1 2
#define CHANNEL_M2_IN2 3

#define DRIVE_FORWARD 0
#define DRIVE_BACKWARD 1
#define DRIVE_FLOAT 2
#define DRIVE_BRAKE 3

// Drive mode when not changing position.
// DRIVE_BRAKE uses H-bridge to inhibit turning.
#define IDLE_MODE DRIVE_FLOAT

static void set_motor_left(const int drive, uint8_t speed);
static void set_motor_right(const int drive, uint8_t speed);

int m1_ticks = 0;
int m2_ticks = 0;

static void IRAM_ATTR hall_isr_m1() {
  if (digitalRead(PIN_M1_HALL_B)) {
    m1_ticks++;
  } else {
    m1_ticks--;
  }
}
static void IRAM_ATTR hall_isr_m2() {
  if (digitalRead(PIN_M2_HALL_B)) {
    m2_ticks++;
  } else {
    m2_ticks--;
  }
}

void motor_init() {
  ledcAttachPin(PIN_M1_IN1, CHANNEL_M1_IN1);
  ledcAttachPin(PIN_M1_IN2, CHANNEL_M1_IN2);
  ledcAttachPin(PIN_M2_IN1, CHANNEL_M2_IN1);
  ledcAttachPin(PIN_M2_IN2, CHANNEL_M2_IN2);
  ledcSetup(CHANNEL_M1_IN1, 20000, 8);
  ledcSetup(CHANNEL_M1_IN2, 20000, 8);
  ledcSetup(CHANNEL_M2_IN1, 20000, 8);
  ledcSetup(CHANNEL_M2_IN2, 20000, 8);
  pinMode(PIN_M1_HALL_A, INPUT);
  pinMode(PIN_M1_HALL_B, INPUT);
  pinMode(PIN_M2_HALL_A, INPUT);
  pinMode(PIN_M2_HALL_B, INPUT);

  // Listen for rising hall sensors
  attachInterrupt(PIN_M1_HALL_A, hall_isr_m1, RISING);
  attachInterrupt(PIN_M2_HALL_A, hall_isr_m2, RISING);
}

/**
 * Set motor speeds directly.
 * -255 = full speed up, 255 = full speed down
 */
void set_motor_speed_raw(short left, short right) {
  // Serial.printf("Set ctrl %d %d\n", left, right);
  left *= config_left_direction;
  right *= config_right_direction;
  if (left < 0) {
    set_motor_left(DRIVE_FORWARD, -left);
  } else if (left > 0) {
    set_motor_left(DRIVE_BACKWARD, left);
  } else {
    set_motor_left(IDLE_MODE, 0);
  }
  if (right < 0) {
    set_motor_right(DRIVE_FORWARD, -right);
  } else if (right > 0) {
    set_motor_right(DRIVE_BACKWARD, right);
  } else {
    set_motor_right(IDLE_MODE, 0);
  }
}

static void set_motor_left(const int drive, uint8_t speed) {
  // Serial.printf("motor speed left %d %d\n", drive, speed);
  if (drive == DRIVE_FORWARD) {
    ledcWrite(CHANNEL_M1_IN1, 0);
    ledcWrite(CHANNEL_M1_IN2, speed);
  } else if (drive == DRIVE_BACKWARD) {
    ledcWrite(CHANNEL_M1_IN1, speed);
    ledcWrite(CHANNEL_M1_IN2, 0);
  } else if (drive == DRIVE_FLOAT) {
    ledcWrite(CHANNEL_M1_IN1, 255);
    ledcWrite(CHANNEL_M1_IN2, 255);
  } else if (drive == DRIVE_BRAKE) {
    ledcWrite(CHANNEL_M1_IN1, 0);
    ledcWrite(CHANNEL_M1_IN2, 0);
  }
}

static void set_motor_right(const int drive, uint8_t speed) {
  // Serial.printf("motor speed right %d %d\n", drive, speed);
  if (drive == DRIVE_FORWARD) {
    ledcWrite(CHANNEL_M2_IN1, speed);
    ledcWrite(CHANNEL_M2_IN2, 0);
  } else if (drive == DRIVE_BACKWARD) {
    ledcWrite(CHANNEL_M2_IN1, 0);
    ledcWrite(CHANNEL_M2_IN2, speed);
  } else if (drive == DRIVE_FLOAT) {
    ledcWrite(CHANNEL_M2_IN1, 255);
    ledcWrite(CHANNEL_M2_IN2, 255);
  } else if (drive == DRIVE_BRAKE) {
    ledcWrite(CHANNEL_M2_IN1, 0);
    ledcWrite(CHANNEL_M2_IN2, 0);
  }
}
