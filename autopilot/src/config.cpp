#include <Arduino.h>
#include <EEPROM.h>
#include "paradrone.h"

#define ADDR_LZ 0 // landing zone
#define ADDR_MC 13 // frequency
#define ADDR_AP 22 // flight mode
#define ADDR_CAL 23 // calibration

#define SIGNUM(x) (x < 0) ? -1 : (x > 0)

// Persisted config
// Flight mode
uint8_t config_flight_mode = MODE_IDLE;

// Landing zone
LandingZone *config_landing_zone;

// Default motor config
MotorConfigMessage motor_config = {
  'C',
  .frequency = (int) LORA_BAND, // Hz
  .stroke = 1200, // millimeters
  .dir = 0
};
// Multiplier, 1 = clockwise, -1 = counterclockwise
short config_direction_left = 1;
short config_direction_right = 1;

// Encoder calibration
int config_ticks_per_second = 1800;

static void load_flight_mode();
static void load_landing_zone();
static void load_motor_config();

void config_init() {
  EEPROM.begin(512);
  load_flight_mode();
  load_landing_zone();
  load_motor_config();
}

/**
 * Load flight mode from EEPROM
 */
static void load_flight_mode() {
  const uint8_t mode = EEPROM.read(ADDR_AP);
  if (mode <= 1) {
    config_flight_mode = mode;
    // Serial.printf("%.1fs flight mode %d\n", millis() * 1e-3, mode);
  }
}

/**
 * Set flight mode and save to EEPROM
 */
void set_flight_mode(uint8_t mode) {
  // Persist to EEPROM
  EEPROM.put(ADDR_AP, mode);
  EEPROM.commit();

  // Update state
  config_flight_mode = mode;
  // Reset R/C override
  last_rc_millis = -RC_OVERRIDE_MILLIS;

  planner_loop(); // handles mode change for flight computer
  screen_update();
}

/**
 * Load landing zone state from EEPROM
 */
static void load_landing_zone() {
  if (EEPROM.read(ADDR_LZ) == 'Z') {
    if (config_landing_zone) {
      delete config_landing_zone;
    }
    LandingZoneMessage packed = {};
    EEPROM.get(ADDR_LZ, packed);
    config_landing_zone = unpack_lz(&packed);
    Serial.printf(
      "LZ %f %f %.1f %.0f°\n",
      config_landing_zone->destination.lat,
      config_landing_zone->destination.lng,
      config_landing_zone->destination.alt,
      to_degrees(config_landing_zone->landingDirection)
    );
  }
}

/**
 * Set landing zone from packed lz message
 */
void set_landing_zone(LandingZoneMessage *packed) {
  // Persist to EEPROM
  EEPROM.put(ADDR_LZ, *packed);
  EEPROM.commit();
  load_landing_zone();
}

/**
 * Load motor config state from EEPROM
 */
static void load_motor_config() {
  if (EEPROM.read(ADDR_MC) == 'C') {
    EEPROM.get(ADDR_MC, motor_config);
    config_direction_left = (motor_config.dir & 1) * 2 - 1;
    config_direction_right = (motor_config.dir & 2) - 1;
    Serial.printf("%.1fs cfg %d S%d L%d R%d\n", millis() * 1e-3, motor_config.frequency, motor_config.stroke, config_direction_left, config_direction_right);
  }
}

/**
 * Save motor config to EEPROM
 */
void set_motor_config(MotorConfigMessage *msg) {
  // Update LoRa frequency
  lora_set_frequency(msg->frequency);
  // Persist to EEPROM
  EEPROM.put(ADDR_MC, *msg);
  EEPROM.commit();
  load_motor_config();
}

/**
 * Save calibration settings to EEPROM
 */
void set_calibration(int ticks_per_second) {
  EEPROM.put(ADDR_CAL, ticks_per_second);
  EEPROM.commit();
  config_ticks_per_second = ticks_per_second;
}
