#include <Arduino.h>
#include "rc.h"

void setup() {
  Serial.begin(115200);
  Serial.println("ParaDroneRC");

  screen_init(); // 70ms
  bt_init(); // 680ms
  lora_init(); // 30ms
}

void loop() {
  lora_loop();
  screen_loop();
  delay(20);
}
