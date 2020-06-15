#include <Arduino.h>
#include <EEPROM.h>
#include "heltec.h"
#include "paradrone.h"

void blink(int count);

#define LORA_BAND 915E6

void setup() {
  Heltec.begin(
    false, // Display
    true, // LoRa
    true, // Serial
    true, // PABOOST
    LORA_BAND
  );
  EEPROM.begin(512);
  // WiFi.mode(WIFI_OFF);
  // Serial.println("ParaDrone");

  load_landing_zone();
  screen_init();
  motor_init();
  gps_init();
  bt_init();
  lora_init();

  // Welcome
  blink(4);
}

void loop() {
  gps_loop();
  planner_loop();
  screen_loop();
  lora_loop();
  delay(20);
}

/**
 * Called when GPS location is updated.
 * This orchestrates the services that depend on location updates.
 */
void update_location(GeoPointV *point) {
  Serial.printf("GPS %f, %f, %.1f\n", point->lat, point->lng, point->alt);
  if (last_location) free(last_location);
  last_location = point;
  last_fix_millis = millis();
  motor_loop();
  screen_update();
  blink(1);
  // Plan and update controls
  planner_update_location(point);
  // log_point(point);
  // Notify listeners
  bt_notify(point);
  lora_send_location(point);
}

/**
 * Blink LED
 */
void blink(int count) {
  digitalWrite(LED, HIGH); // LED on
  delay(50);
  digitalWrite(LED, LOW); // LED off
  for (int i = 1; i < count; i++) {
    delay(50);
    digitalWrite(LED, HIGH); // LED on
    delay(50);
    digitalWrite(LED, LOW); // LED off
  }
}
