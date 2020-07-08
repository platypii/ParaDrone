#include <Arduino.h>
#include <EEPROM.h>
#include "heltec.h"
#include "paradrone.h"

#define TIME_START long start_time = millis()
#define TIME_END long time_delta = millis() - start_time; if (time_delta >= 100) Serial.printf("Slow loop %ld %ldms thread %d\n", millis(), time_delta, xPortGetCoreID())

void blink(int count);

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
  TIME_START;
  gps_loop();
  planner_loop();
  motor_loop();
  screen_loop();
  lora_loop();
  TIME_END;
  delay(50);
}

/**
 * Called when GPS location is updated.
 * This orchestrates the services that depend on location updates.
 */
void update_location(GeoPointV *point) {
  Serial.printf("GPS %.1fs %f, %f, %.1f, %.1f m/s\n", millis() * 1e-3, point->lat, point->lng, point->alt, hypot(point->vE, point->vN));
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
  bt_send_location(point);
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