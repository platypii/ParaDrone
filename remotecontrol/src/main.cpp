#include <heltec.h>
#include "rc.h"

void setup() {
  Heltec.begin(
    false, // Display
    false, // LoRa
    true, // Serial
    true, // PABOOST
    LORA_BAND
  ); // 50ms
  screen_init(); // 200ms
  bt_init(); // 680ms
  lora_init(); // 70ms
}

void loop() {
  lora_loop();
  screen_loop();
  delay(20);
}
