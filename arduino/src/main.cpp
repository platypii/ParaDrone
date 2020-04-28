#include <Arduino.h>
#include "paradrone.h"

// Use ESP32 secondary serial port, since primary interferes with programming
#define RXD2 16
#define TXD2 17

#define LED_BUILTIN 2
void blink(int count);

void setup() {
  // Serial debugging
  // Serial.begin(115200);
  Serial.begin(9600);
  Serial.println("ParaDrone");

  // Init GPS
  Serial2.begin(9600, SERIAL_8N1, RXD2, TXD2);

  // Init BLE
  bt_init();

  // Init LED
  pinMode(LED_BUILTIN, OUTPUT);
  blink(4);
}

void loop() {
  read_gps();
  delay(100); // wait a bit
}

/**
 * Called when GPS location is updated
 */
void update_location(GeoPointV point) {
  Serial.printf("GPS %f, %f, %.1f\n", point.lat, point.lng, point.alt);
  blink(1);
  bt_notify(point);
  // log_point(point);
  // TODO: Plan and update controls
}

/**
 * Blink LED
 */
void blink(int count) {
  digitalWrite(LED_BUILTIN, HIGH); // LED on
  delay(50);
  digitalWrite(LED_BUILTIN, LOW); // LED off
  for (int i = 1; i < count; i++) {
    delay(50);
    digitalWrite(LED_BUILTIN, HIGH); // LED on
    delay(50);
    digitalWrite(LED_BUILTIN, LOW); // LED off
  }
}
