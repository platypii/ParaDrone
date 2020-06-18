#ifndef _PARADRONE_H
#define _PARADRONE_H

#include "dtypes.h"
#include "geo.h"
#include "landingzone.h"

#define PARAMOTOR_GROUNDSPEED 11
#define PARAMOTOR_DESCENTRATE 4
#define PARAMOTOR_TURNRADIUS 100
#define PARAMOTOR_GLIDE (PARAMOTOR_GROUNDSPEED / PARAMOTOR_DESCENTRATE)

// LoRa North America
#define LORA_BAND 915E6

// Global vars
extern LandingZone *current_landing_zone;
extern GeoPointV *last_location;
extern long last_fix_millis;
extern bool bt_connected;
extern bool lora_enabled;

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

// LoRa
void lora_init();
void lora_loop();
void lora_send_location(GeoPointV *point);

// Landing zone
void load_landing_zone();
void set_landing_zone(const char *packed);

ParaControls path_controls(Path *path);

// Motors
void motor_init();
void motor_loop();
void set_motor_controls(const short left, const short right);
void set_motor_position(uint8_t new_left, uint8_t new_right);

// Planner
void planner_loop();
void planner_update_location(GeoPointV *point);
double plan_score(LandingZone *lz, Path *plan);
void rc_set_position(uint8_t new_left, uint8_t new_right);

double flight_distance_remaining(const double alt);
Path *search(Point3V loc3, LandingZone *lz, const double r);

#endif
