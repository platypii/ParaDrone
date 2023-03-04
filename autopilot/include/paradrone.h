#ifndef _PARADRONE_H
#define _PARADRONE_H

#include "geo.h"
#include "landingzone.h"
#include "messages.h"
#include "motor.h"

// LoRa North America
#define LORA_BAND 915E6

// Flight modes
#define MODE_IDLE 0
#define MODE_AUTO 1

// Revert manual control to autopilot after 10 seconds of no RC
#define RC_OVERRIDE_MILLIS 10000
// In case of speed override, only 3 seconds
#define RC_SPEED_OVERRIDE_DURATION 3000

// After 60 seconds of no GPS, revert to slow spiral
#define GPS_EXPIRATION 60000

// Error message
extern const char *error;

// Persisted config
extern uint8_t config_flight_mode;
extern LandingZone *config_landing_zone;
extern MotorConfigMessage motor_config;

// Motor direction
extern short config_direction_left;
extern short config_direction_right;

// Encoder calibration
extern int config_ticks_per_second;

// Last R/C message received
extern long last_rc_millis;
extern long last_speed_override;

// AP state
extern GeoPointV *last_location;
extern long last_fix_millis;
extern bool bt_connected;
extern bool lora_enabled;
extern uint8_t flight_mode;
extern Path *current_plan;
extern const char *current_plan_name;
bool rc_override();

// Left and Right motors
extern Motor *motor_left;
extern Motor *motor_right;

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
void bt_send_url(const char *url);
void bt_send_calibration(uint16_t left1, uint16_t left2, uint16_t right1, uint16_t right2);

// GPS
void gps_init();
void gps_loop();
void ublox_init();
void parse_nmea(char *line);
void update_location(GeoPointV *point);

// Logger
void log_point(GeoPointV *loc);

// LoRa
void lora_init();
void lora_loop();
void lora_send_location(GeoPointV *point);
void lora_send_lz();
void lora_set_frequency(long frequency);

// Config
void config_init();
void set_landing_zone(LandingZoneMessage *packed);
void set_motor_config(MotorConfigMessage *msg);
void set_calibration(int ticks_per_second);
void calibrate();

// Motors
void motor_init();
void motor_loop();
void set_motor_speeds(const short left, const short right);
void set_toggles(uint8_t new_left, uint8_t new_right);

// Planner
void planner_loop();
void planner_update_location(GeoPointV *point);
double plan_score(LandingZone *lz, Path *plan);
TogglePosition path_controls(Path *path);

// Flight computer
void set_flight_mode(uint8_t mode);
void rc_set_speed(const short new_left, const short new_right);
void rc_set_position(uint8_t new_left, uint8_t new_right);

// Web server
void web_init(const char *ssid, const char *password);
void web_loop();

#endif
