#include <Arduino.h>
#include "heltec.h"
#include "relay.h"

void setup() {
  Heltec.begin(
    false, // Display
    true, // LoRa
    true, // Serial
    true, // PABOOST
    LORA_BAND
  );
  screen_init();
  bt_init();
  lora_init();
}

void loop() {
  lora_loop();
  screen_loop();
  delay(20);
}
