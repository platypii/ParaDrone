#include <SSD1306Wire.h>
#include "rc.h"

static bool should_redraw = false;
static unsigned long last_redraw_millis = 2000; // splash screen ms
static void screen_draw();
static void sprintd(char *buf, long delta);

SSD1306Wire display(0x3c, SDA_OLED, SCL_OLED);

char buf[40];

void screen_init() {
  // Reset needed for heltec screen
  pinMode(RST_OLED, OUTPUT);
  digitalWrite(RST_OLED, LOW);
  delay(20);
  digitalWrite(RST_OLED, HIGH);

  display.init();
  display.flipScreenVertically();
  display.setTextAlignment(TEXT_ALIGN_LEFT);
  display.setFont(ArialMT_Plain_24);
  display.drawString(6, 19, "ParaDrone");
  display.setFont(ArialMT_Plain_10);
  display.drawString(7, 12, "BASEline");
  display.drawString(86, 40, "<=> RC");
  display.display();
}

void screen_loop() {
  // Refresh at least once per second
  if (millis() >= last_redraw_millis + 1000) {
    should_redraw = true;
  }
  if (should_redraw) {
    screen_draw();
    last_redraw_millis = millis();
    should_redraw = false;
  }
}

/**
 * Notify that screen should redraw on next loop. Non-blocking
 */
void screen_update() {
  should_redraw = true;
}

static void screen_draw() {
  display.clear();
  display.setTextAlignment(TEXT_ALIGN_LEFT);

  if (last_lat != 0 && last_lng != 0) {
    // Show last lat,lng to help find lost drone
    sprintf(buf, "%f, %f", last_lat, last_lng);
    display.drawString(0, 0, buf);
  } else {
    display.drawString(20, 0, "ParaDrone <=> RC");
  }

  // Alt
  if (!isnan(last_alt)) {
    sprintf(buf, "%.0f m MSL", last_alt);
    display.drawString(0, 10, buf);
  }

  // LoRa lastfix
  if (last_packet_millis > 0) {
    long delta = millis() - last_packet_millis;

    if (delta <= 15000) {
      sprintf(buf, "Snr %.2f", last_packet_snr);
      display.drawString(0, 30, buf);

      sprintf(buf, "Rssi %d", last_packet_rssi);
      display.drawString(0, 42, buf);

      display.drawString(0, 54, "LoRa");
    }
  }

  // GPS lastfix
  display.setTextAlignment(TEXT_ALIGN_RIGHT);
  if (last_fix_millis >= 0) {
    const long delta = millis() - last_fix_millis;
    sprintd(buf, delta);
    display.drawString(DISPLAY_WIDTH, 10, buf);
  }

  // Phone connected?
  if (bt_connected) {
    display.drawString(DISPLAY_WIDTH, 54, "BT");
  }

  // Battery level
  sprintf(buf, "%.0f%%", 100 * get_battery_level());
  display.drawString(DISPLAY_WIDTH, 44, buf);

  display.display();
}

/**
 * Print time duration to a buffer (2h, 5m, 6s)
 * @param buf buffer to write to
 * @param delta time duration in milliseconds
 */
static void sprintd(char *buf, long delta) {
  if (delta < 1100) {
    sprintf(buf, "0s");
  } else if (delta < 60000) {
    sprintf(buf, "%lds", delta / 1000);
  } else if (delta < 3600000) {
    sprintf(buf, "%ldm", delta / 60000);
  } else if (delta < 86400000) {
    sprintf(buf, "%ldh", delta / 3600000);
  }
}
