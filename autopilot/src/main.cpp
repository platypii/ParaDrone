#include <Arduino.h>
#include <EEPROM.h>
#include "paradrone.h"

#define TIME_START long start_time = millis()
#define TIME_STEP(name) if (millis() - start_time >= 80) Serial.printf("%.1fs slow %s %ldms thread %d\n", millis() * 1e-3, name, millis() - start_time, xPortGetCoreID()); start_time = millis()

void setup() {
  Serial.begin(115200);
  Serial.println("ParaDrone");
  // WiFi.mode(WIFI_OFF);

  config_init();
  gps_init();
  motor_init();
  screen_init(); // 70ms
  bt_init(); // 680ms
  lora_init(); // 30ms
  // web_init("ssid", "password");
}

void loop() {
  TIME_START;
  gps_loop();
  TIME_STEP("gps");
  planner_loop();
  TIME_STEP("plan");
  motor_loop();
  TIME_STEP("motor");
  screen_loop();
  TIME_STEP("screen");
  lora_loop();
  TIME_STEP("lora");
  web_loop();
  TIME_STEP("web");
  // TODO: Sleep exactly enough to be ready for next gps
  delay(80);
}

/**
 * Called when GPS location is updated.
 * This orchestrates the services that depend on location updates.
 */
void update_location(GeoPointV *point) {
  Serial.printf("%.1fs gps %f, %f, %.1f, %.1f m/s\n", millis() * 1e-3, point->lat, point->lng, point->alt, hypot(point->vE, point->vN));
  if (last_location) free(last_location);
  last_location = point;
  last_fix_millis = millis();
  screen_update();
  // Plan and update controls
  planner_update_location(point);
  motor_loop();
  // Notify listeners
  bt_send_location(point);
  lora_send_location(point);
  // log_point(point); // TODO: Logging causing constant crashing. Move to core 1?
}
