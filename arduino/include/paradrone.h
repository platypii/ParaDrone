#ifndef _PARADRONE_H
#define _PARADRONE_H

#include "dtypes.h"
#include "geo.h"

#define PARAMOTOR_GROUNDSPEED 11
#define PARAMOTOR_DESCENTRATE 4
#define PARAMOTOR_TURNRADIUS 100

// Global vars
extern LandingZone *current_landing_zone;
extern GeoPointV *last_location;
extern long last_fix_millis;
extern bool bt_connected;

// Current motor position
extern short motor_position_left;
extern short motor_position_right;

// Target motor position
extern short motor_target_left;
extern short motor_target_right;

// Screen
void screen_init();
void screen_loop();
void screen_update();

// Bluetooth
void bt_init();
void bt_notify(GeoPointV *point);

// GPS
void gps_init();
void gps_loop();
void parse_nmea(char *line);
void update_location(GeoPointV *point);

// Logger
void log_point(GeoPointV *loc);

// Math
double mod360(double degrees);
double to_degrees(double radians);
double to_radians(double degrees);

// Landing zone
void load_landing_zone();
void set_landing_zone(const char *packed);

// Motors
void motor_init();
void motor_loop();
void set_controls(const short left, const short right);
void set_position(uint8_t new_left, uint8_t new_right);

#endif
