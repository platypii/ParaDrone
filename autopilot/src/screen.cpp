#include <heltec.h>
#include "paradrone.h"

static bool should_redraw = false;
static long last_redraw_millis = -1;
static void screen_draw();
static void sprintd(char *buf, long delta);

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

  if (last_location != NULL) {
    // Lat/lng
    sprintf(buf, "%f, %f", last_location->lat, last_location->lng);
    Heltec.display->setTextAlignment(TEXT_ALIGN_LEFT);
    Heltec.display->drawString(0, 2, buf);
  } else {
    Heltec.display->setTextAlignment(TEXT_ALIGN_CENTER);
    Heltec.display->drawString(DISPLAY_WIDTH / 2, 2, "ParaDrone");
    Heltec.display->setTextAlignment(TEXT_ALIGN_LEFT);
  }

  // Alt
  if (last_location != NULL && !isnan(last_location->alt)) {
    if (config_landing_zone != NULL) {
      const double alt = last_location->alt - config_landing_zone->destination.alt;
      sprintf(buf, "%.0f mAGL", alt);
    } else {
      sprintf(buf, "%.0f mMSL", last_location->alt);
    }
    Heltec.display->drawString(0, 12, buf);
  }

  // Ground speed
  if (last_location != NULL) {
    const double vel = sqrt(last_location->vN * last_location->vN + last_location->vE * last_location->vE);
    if (!isnan(vel)) {
      sprintf(buf, "%.0f mph", vel * 2.23694);
      Heltec.display->drawString(70, 12, buf);
   }
  }

  // GPS lastfix
  Heltec.display->setTextAlignment(TEXT_ALIGN_RIGHT);
  if (last_fix_millis >= 0) {
    long delta = millis() - last_fix_millis;
    sprintd(buf, delta);
    Heltec.display->drawString(DISPLAY_WIDTH, 12, buf);
  }

  // LZ
  if (config_landing_zone != NULL && last_location != NULL) {
    const double distance = geo_distance(last_location->lat, last_location->lng, config_landing_zone->destination.lat, config_landing_zone->destination.lng);
    if (distance >= 100000) {
      sprintf(buf, "LZ %.0f km ", distance * 1e-3);
    } else if (distance >= 10000) {
      sprintf(buf, "LZ %.1f km ", distance * 1e-3);
    } else {
      sprintf(buf, "LZ %.0f m ", distance);
    }
    const double bearing = geo_bearing(last_location->lat, last_location->lng, config_landing_zone->destination.lat, config_landing_zone->destination.lng);
    bearing2(buf + strnlen(buf, 20), to_degrees(bearing));
  } else if (config_landing_zone != NULL) {
    sprintf(buf, "LZ %.1f, %.1f, %.0fm", config_landing_zone->destination.lat, config_landing_zone->destination.lng, config_landing_zone->destination.alt);
  } else {
    sprintf(buf, "No LZ");
  }
  Heltec.display->drawString(DISPLAY_WIDTH, 22, buf);

  // Flight mode
  Heltec.display->setTextAlignment(TEXT_ALIGN_LEFT);
  if (rc_override()) {
    Heltec.display->drawString(0, 22, "RC");
  } else if (config_flight_mode == MODE_IDLE) {
    Heltec.display->drawString(0, 22, "Idle");
  } else if (config_flight_mode == MODE_AP) {
    if (current_plan_name) {
      Heltec.display->drawString(0, 22, current_plan_name);
    } else {
      Heltec.display->drawString(0, 22, "Auto");
    }
  }

  // Limit switch state
  if (get_motor_switch_left() > 64) {
    Heltec.display->drawString(0, -8, "_");
  }
  if (get_motor_switch_right() > 64) {
    Heltec.display->drawString(122, -8, "_");
  }

  // Current motor position
  sprintf(buf, "%d ", (short) motor_position_left);
  int prewidth = Heltec.display->getStringWidth(buf);
  sprintf(buf, "%d | %d", (short) motor_position_left, (short) motor_position_right);
  Heltec.display->drawString(DISPLAY_WIDTH / 2 - prewidth, 54, buf);
  // // Target position
  // if (motor_position_left != motor_target_left || motor_position_right != motor_target_right) {
  //   sprintf(buf, "%d ", motor_target_left);
  //   prewidth = Heltec.display->getStringWidth(buf);
  //   sprintf(buf, "%d | %d", motor_target_left, motor_target_right);
  //   Heltec.display->drawString(DISPLAY_WIDTH / 2 - prewidth, 44, buf);
  // }
  // Motor current
  sprintf(buf, "%.2fA ", motor_current_left);
  prewidth = Heltec.display->getStringWidth(buf);
  sprintf(buf, "%.2fA | %.2fA", motor_current_left, motor_current_right);
  Heltec.display->drawString(DISPLAY_WIDTH / 2 - prewidth, 44, buf);

  // LoRa enabled?
  if (lora_enabled) {
    Heltec.display->drawString(0, 54, "LoRa");
  }

  // Phone connected?
  if (bt_connected) {
    Heltec.display->setTextAlignment(TEXT_ALIGN_RIGHT);
    Heltec.display->drawString(DISPLAY_WIDTH, 54, "BT");
  }

  Heltec.display->display();
}

/**
 * Print time duration to a buffer (2h, 5m, 6s)
 * @param delta time duration in milliseconds
 */
static void sprintd(char *buf, long delta) {
  if (delta < 60000) {
    sprintf(buf, "%lds", delta / 1000);
  } else if (delta < 3600000) {
    sprintf(buf, "%ldm", delta / 60000);
  } else if (delta < 86400000) {
    sprintf(buf, "%ldh", delta / 3600000);
  }
}
