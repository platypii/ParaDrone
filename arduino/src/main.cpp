#include <Arduino.h>
#include <EEPROM.h>
#include "heltec.h"
#include "paradrone.h"

void blink(int count);

#define LORA_BAND 915E6

void setup() {
  Heltec.begin(
    true, // Display
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
  init_gps();
  bt_init();

  // Welcome
  blink(4);
}

void loop() {
  read_gps();
  screen_loop();
  delay(20);
}

/**
 * Called when GPS location is updated
 */
void update_location(GeoPointV *point) {
  // Serial.printf("GPS %f, %f, %.1f\n", point.lat, point.lng, point.alt);
  if (last_location) free(last_location);
  last_location = point;
  screen_update();
  blink(1);
  bt_notify(point);
  // log_point(point);
  // TODO: Plan and update controls
  // TODO: Free point
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
