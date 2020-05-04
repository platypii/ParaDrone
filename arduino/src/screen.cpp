#include "heltec.h"
#include "paradrone.h"

static boolean should_redraw = false;
static long last_redraw_millis = 0;
static void screen_draw();

void screen_init() {
  Wire.begin(SDA_OLED, SCL_OLED); //Scan OLED's I2C address via I2C0
  Heltec.display->clear();
  Heltec.display->setTextAlignment(TEXT_ALIGN_LEFT);
  Heltec.display->setFont(ArialMT_Plain_10);
  Heltec.display->drawString(3, 12, "BASEline");
  Heltec.display->setFont(ArialMT_Plain_24);
  Heltec.display->drawString(2, 19, "ParaDrone");
  Heltec.display->setFont(ArialMT_Plain_16);
  Heltec.display->drawString(62, 41, "AutoPilot");
  Heltec.display->display();
}

void screen_loop() {
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

  if (last_location != NULL) {
    // Lat/lng
    sprintf(buf, "%f, %f", last_location->lat, last_location->lng);
    Heltec.display->setTextAlignment(TEXT_ALIGN_LEFT);
    Heltec.display->drawString(0, 0, buf);
  } else {
    Heltec.display->setTextAlignment(TEXT_ALIGN_CENTER);
    Heltec.display->drawString(DISPLAY_WIDTH / 2, 0, "ParaDrone");
    Heltec.display->setTextAlignment(TEXT_ALIGN_LEFT);
  }

  // LZ
  if (current_landing_zone != NULL && last_location != NULL) {
    const double distance = geo_distance(last_location->lat, last_location->lng, current_landing_zone->destination.lat, current_landing_zone->destination.lng);
    sprintf(buf, "LZ: %.0f m  ", distance);
    const double bearing = geo_bearing(last_location->lat, last_location->lng, current_landing_zone->destination.lat, current_landing_zone->destination.lng);
    bearing2(buf + strnlen(buf, 20), to_degrees(bearing));
  } else if (current_landing_zone != NULL) {
    sprintf(buf, "LZ: Set");
  } else {
    sprintf(buf, "LZ:");
  }
  Heltec.display->drawString(0, 10, buf);

  // Alt
  if (last_location != NULL && !isnan(last_location->alt)) {
    if (current_landing_zone != NULL) {
      const double alt = last_location->alt - current_landing_zone->destination.alt;
      sprintf(buf, "Alt: %.1f m AGL", alt);
    } else {
      sprintf(buf, "Alt: %.1f m MSL", last_location->alt);
    }
  } else {
    sprintf(buf, "Alt:");
  }
  Heltec.display->drawString(0, 20, buf);

  // Controls
  sprintf(buf, "%d | %d", control_left, control_right);
  Heltec.display->setTextAlignment(TEXT_ALIGN_CENTER);
  Heltec.display->drawString(DISPLAY_WIDTH / 2, 54, buf);

  // Phone connected?
  if (bt_connected) {
    Heltec.display->setTextAlignment(TEXT_ALIGN_RIGHT);
    Heltec.display->drawString(DISPLAY_WIDTH, 54, "BT");
  }

  Heltec.display->display();
}
