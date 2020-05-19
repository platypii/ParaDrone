#include "heltec.h"
#include "relay.h"

static boolean should_redraw = false;
static long last_redraw_millis = 0;
static void screen_draw();

void screen_init() {
  Heltec.display->init();
  Heltec.display->flipScreenVertically();
  Heltec.display->setTextAlignment(TEXT_ALIGN_LEFT);
  Heltec.display->setFont(ArialMT_Plain_10);
  Heltec.display->drawString(3, 12, "BASEline");
  Heltec.display->setFont(ArialMT_Plain_24);
  Heltec.display->drawString(2, 19, "ParaDrone");
  Heltec.display->setFont(ArialMT_Plain_16);
  Heltec.display->drawString(54, 41, "<=> Relay");
  Heltec.display->display();
}

void screen_loop() {
  // Refresh at least once per second
  if (last_redraw_millis > 0 && millis() - last_redraw_millis >= 1000) {
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
    Heltec.display->drawString(14, 0, "ParaDrone <=> Relay");
  }

  if (last_packet_millis > 0) {
    long delta = millis() - last_packet_millis;

    if (delta <= 5000) {
      sprintf(buf, "SNR %.2f", last_packet_snr);
      Heltec.display->drawString(0, 30, buf);

      sprintf(buf, "RSSI %d", last_packet_rssi);
      Heltec.display->drawString(0, 42, buf);
    }

    if (delta <= 60 * 60 * 1000) {
      sprintf(buf, "LoRa %lds", delta / 1000);
      Heltec.display->drawString(0, 54, buf);
    }
  }

  // Phone connected?
  if (bt_connected) {
    Heltec.display->drawString(115, 54, "BT");
  }

  Heltec.display->display();
}
