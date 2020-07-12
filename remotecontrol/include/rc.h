#ifndef _RC_H
#define _RC_H

// LoRa North America
#define LORA_BAND 915E6

// Global vars
extern bool bt_connected;

// LoRa
extern long last_packet_millis;
extern int last_packet_rssi;
extern float last_packet_snr;

extern double last_lat;
extern double last_lng;
extern float last_alt;
extern long last_fix_millis;

void screen_init();
void screen_loop();
void screen_update();

void bt_init();
void bt_notify(uint8_t *data, size_t len);

void lora_init();
void lora_loop();
void lora_send(uint8_t *data, size_t len);

float get_battery_level();

/**
 * Message with just location.
 * sizeof 11
 */
#pragma pack(1)
struct LocationMessage {
  char msg_type; // 'L'
  int lat; // microdegrees
  int lng; // microdegrees
  short alt; // decimeters
};

#endif
