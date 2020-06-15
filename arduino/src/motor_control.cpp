#include <Arduino.h>
#include "heltec.h"

#define PIN_M1_IN1 2
#define PIN_M1_IN2 17
#define PIN_M2_IN1 22
#define PIN_M2_IN2 23

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
// Brake uses H-bridge to inhibit turning.
#define IDLE_MODE DRIVE_BRAKE

static void set_motor1(const int drive, uint8_t speed);
static void set_motor2(const int drive, uint8_t speed);

void motor_init() {
  ledcAttachPin(PIN_M1_IN1, CHANNEL_M1_IN1);
  ledcAttachPin(PIN_M1_IN2, CHANNEL_M1_IN2);
  ledcAttachPin(PIN_M2_IN1, CHANNEL_M2_IN1);
  ledcAttachPin(PIN_M2_IN2, CHANNEL_M2_IN2);
  ledcSetup(CHANNEL_M1_IN1, 20000, 8);
  ledcSetup(CHANNEL_M1_IN2, 20000, 8);
  ledcSetup(CHANNEL_M2_IN1, 20000, 8);
  ledcSetup(CHANNEL_M2_IN2, 20000, 8);
}

/**
 * Set motor controls.
 * -255 = full speed down, 255 = full speed up
 */
void set_controls(const short left, const short right) {
  // Serial.printf("Set ctrl %d %d\n", left, right);
  if (left < 0) {
    set_motor2(DRIVE_FORWARD, -left);
  } else if (left > 0) {
    set_motor2(DRIVE_BACKWARD, left);
  } else {
    set_motor2(IDLE_MODE, 0);
  }
  if (right < 0) {
    set_motor1(DRIVE_FORWARD, -right);
  } else if (right > 0) {
    set_motor1(DRIVE_BACKWARD, right);
  } else {
    set_motor1(IDLE_MODE, 0);
  }
}

static void set_motor1(const int drive, uint8_t speed) {
  // Serial.printf("Set motor1 %d %d\n", drive, speed);
  if (drive == DRIVE_FORWARD) {
    ledcWrite(CHANNEL_M1_IN1, speed);
    ledcWrite(CHANNEL_M1_IN2, 0);
  } else if (drive == DRIVE_BACKWARD) {
    ledcWrite(CHANNEL_M1_IN1, 0);
    ledcWrite(CHANNEL_M1_IN2, speed);
  } else if (drive == DRIVE_FLOAT) {
    ledcWrite(CHANNEL_M1_IN1, 255);
    ledcWrite(CHANNEL_M1_IN2, 255);
  } else if (drive == DRIVE_BRAKE) {
    ledcWrite(CHANNEL_M1_IN1, 0);
    ledcWrite(CHANNEL_M1_IN2, 0);
  }
}

static void set_motor2(const int drive, uint8_t speed) {
  // Serial.printf("Set motor2 %d %d\n", drive, speed);
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
