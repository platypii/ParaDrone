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
extern bool bt_connected;

extern signed char control_left;
extern signed char control_right;

// Screen
void screen_init();
void screen_loop();
void screen_update();

// Bluetooth
void bt_init();
void bt_notify(GeoPointV *point);

// GPS
void init_gps();
void read_gps();
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
void set_controls(signed char left, signed char right);

#endif
