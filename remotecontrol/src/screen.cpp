#include "heltec.h"
#include "relay.h"

static boolean should_redraw = false;
static long last_redraw_millis = -1;
static void screen_draw();
static void sprintd(char *buf, long delta);

void screen_init() {
  Heltec.display->init();
  Heltec.display->flipScreenVertically();
  Heltec.display->setTextAlignment(TEXT_ALIGN_LEFT);
  Heltec.display->setFont(ArialMT_Plain_10);
  Heltec.display->drawString(7, 12, "BASEline");
  Heltec.display->setFont(ArialMT_Plain_24);
  Heltec.display->drawString(6, 19, "ParaDrone");
  Heltec.display->setFont(ArialMT_Plain_16);
  Heltec.display->drawString(66, 40, "<=> RC");
  Heltec.display->display();
}

void screen_loop() {
  // Refresh at least once per second
  if (last_redraw_millis >= 0 && millis() - last_redraw_millis >= 1000) {
    should_redraw = true;
  }
  if (should_redraw) {
    screen_draw();
    last_redraw_millis = millis();
    should_redraw = false;
  }
}

void screen_update() {
  should_redraw = true;
}

static void screen_draw() {
  char buf[80];
  Heltec.display->clear();
  Heltec.display->setFont(ArialMT_Plain_10);

  if (last_lat != 0 && last_lng != 0) {
    // Show last lat,lng to help find lost drone
    sprintf(buf, "%f, %f", last_lat, last_lng);
    Heltec.display->drawString(0, 0, buf);
  } else {
    Heltec.display->drawString(20, 0, "ParaDrone <=> RC");
  }

  // Alt
  if (!isnan(last_alt)) {
    sprintf(buf, "Alt: %.0f m MSL", last_alt);
    Heltec.display->drawString(0, 10, buf);
  }

  // GPS lastfix
  if (last_fix_millis >= 0) {
    long delta = millis() - last_fix_millis;
    Heltec.display->setTextAlignment(TEXT_ALIGN_RIGHT);
    sprintd(buf, delta);
    Heltec.display->drawString(DISPLAY_WIDTH, 10, buf);
    Heltec.display->setTextAlignment(TEXT_ALIGN_LEFT);
  }

  if (last_packet_millis > 0) {
    long delta = millis() - last_packet_millis;

    if (delta <= 15000) {
      sprintf(buf, "Snr %.2f", last_packet_snr);
      Heltec.display->drawString(0, 30, buf);

      sprintf(buf, "Rssi %d", last_packet_rssi);
      Heltec.display->drawString(0, 42, buf);

      Heltec.display->drawString(0, 54, "LoRa");
    }
  }

  // Phone connected?
  if (bt_connected) {
    Heltec.display->drawString(115, 54, "BT");
  }

  Heltec.display->display();
}

/**
 * Print time duration to a buffer (2h, 5m, 6s)
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
