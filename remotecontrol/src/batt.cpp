#include <heltec.h>

#define PIN_BATT 37 // 13

short get_battery_level() {
  const uint16_t analog = analogRead(PIN_BATT);
  return analog;
}
