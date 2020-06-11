#include "heltec.h"
#include "paradrone.h"

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
  Heltec.display->drawString(62, 41, "AutoPilot");
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
  Heltec.display->drawString(0, 10, buf);

  // GPS lastfix if less than 1 hour
  long delta = millis() - last_fix_millis;
  Heltec.display->setTextAlignment(TEXT_ALIGN_RIGHT);
  if (delta < 60000) {
    sprintf(buf, "%lds", delta / 1000);
    Heltec.display->drawString(DISPLAY_WIDTH, 10, buf);
  } else if (delta < 3600000) {
    sprintf(buf, "%ldm", delta / 60000);
    Heltec.display->drawString(DISPLAY_WIDTH, 10, buf);
  } else if (delta < 86400000) {
    sprintf(buf, "%ldh", delta / 3600000);
    Heltec.display->drawString(DISPLAY_WIDTH, 10, buf);
  }
  Heltec.display->setTextAlignment(TEXT_ALIGN_LEFT);

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
  Heltec.display->drawString(0, 20, buf);

  // Current motor position
  sprintf(buf, "%d ", motor_position_left);
  int prewidth = Heltec.display->getStringWidth(buf);
  sprintf(buf, "%d | %d", motor_position_left, motor_position_right);
  Heltec.display->drawString(DISPLAY_WIDTH / 2 - prewidth, 54, buf);
  // Target position
  if (motor_position_left != motor_target_left || motor_position_right != motor_target_right) {
    sprintf(buf, "%d ", motor_target_left);
    prewidth = Heltec.display->getStringWidth(buf);
    sprintf(buf, "%d | %d", motor_target_left, motor_target_right);
    Heltec.display->drawString(DISPLAY_WIDTH / 2 - prewidth, 44, buf);
  }

  // Phone connected?
  if (bt_connected) {
    Heltec.display->setTextAlignment(TEXT_ALIGN_RIGHT);
    Heltec.display->drawString(DISPLAY_WIDTH, 54, "BT");
  }

  Heltec.display->display();
}
