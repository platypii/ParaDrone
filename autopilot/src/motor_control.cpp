#include <heltec.h>

// Right motor
#define PIN_M1_IN1 23
#define PIN_M1_IN2 22
// Left motor
#define PIN_M2_IN1 17
#define PIN_M2_IN2 2

// Current sense
#define PIN_M1_FB 39
#define PIN_M2_FB 35

// Status flag
#define PIN_M1_SF 38
#define PIN_M2_SF 34

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
#define IDLE_MODE DRIVE_FLOAT

static void set_motor_left(const int drive, uint8_t speed);
static void set_motor_right(const int drive, uint8_t speed);

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
 * Set motor speeds directly.
 * -255 = full speed up, 255 = full speed down
 */
void set_motor_speed_raw(const short left, const short right) {
  // Serial.printf("Set ctrl %d %d\n", left, right);
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

static void set_motor_right(const int drive, uint8_t speed) {
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

static void set_motor_left(const int drive, uint8_t speed) {
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

/**
 * Return the current in amps for motor 1
 */
float get_motor_current_right() {
  // 0..4096 => 0..3.3V @ 525 mV per amp
  const uint16_t analog = analogRead(PIN_M1_FB);
  const float volts = analog * 3.3f / 4096.0f;
  return volts / 0.525f;
}

/**
 * Return the current in amps for motor 2
 */
float get_motor_current_left() {
  // 0..4096 => 0..3.3V @ 525 mV per amp
  const uint16_t analog = analogRead(PIN_M2_FB);
  const float volts = analog * 3.3f / 4096.0f;
  return volts / 0.525f;
}
