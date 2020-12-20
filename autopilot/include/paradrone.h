#ifndef _PARADRONE_H
#define _PARADRONE_H

#include "geo.h"
#include "landingzone.h"
#include "messages.h"

// LoRa North America
#define LORA_BAND 915E6

// Flight modes
#define MODE_IDLE 0
#define MODE_AP 1

// Persisted config
extern uint8_t config_flight_mode;
extern LandingZone *config_landing_zone;
extern MotorConfigMessage motor_config;

extern short config_left_invert;
extern short config_right_invert;

// AP state
extern GeoPointV *last_location;
extern long last_fix_millis;
extern bool bt_connected;
extern bool lora_enabled;
extern uint8_t flight_mode; // TODO: Persist

// Current motor position
extern float motor_position_left;
extern float motor_position_right;
// Target motor position
extern short motor_target_left;
extern short motor_target_right;
// Motor current
extern float motor_current_left;
extern float motor_current_right;

// Toggle speed and left/right balance
float get_turn_speed();
float get_turn_balance();

// Screen
void screen_init();
void screen_loop();
void screen_update();

// Bluetooth
void bt_init();
void bt_send_location(GeoPointV *point);

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
void lora_send_lz();

// Config
void config_init();
void set_landing_zone(LandingZoneMessage *packed);
void set_motor_config(MotorConfigMessage *msg);

// Motors
void motor_init();
void motor_loop();
void set_motor_speed(const short left, const short right);
void set_motor_position(uint8_t new_left, uint8_t new_right);
int get_motor_switch_left();
int get_motor_switch_right();

// Planner
void planner_loop();
void rc_set_position(uint8_t new_left, uint8_t new_right);
void planner_update_location(GeoPointV *point);
double plan_score(LandingZone *lz, Path *plan);
ParaControls path_controls(Path *path);

// Flight computer
void set_flight_mode(uint8_t mode);
void rc_set_speed(const short new_left, const short new_right);
void rc_set_position(uint8_t new_left, uint8_t new_right);

#endif
