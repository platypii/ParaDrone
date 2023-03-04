#include <Arduino.h>
#include "rc.h"

void setup() {
  Serial.begin(115200);
  Serial.println("ParaDrone");

  screen_init(); // 200ms
  bt_init(); // 680ms
  lora_init(); // 70ms
}

void loop() {
  lora_loop();
  screen_loop();
  delay(20);
}
