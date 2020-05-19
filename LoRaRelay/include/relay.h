#ifndef _RELAY_H
#define _RELAY_H

// LoRa North America
#define LORA_BAND 915E6

// Global vars
extern bool bt_connected;

// LoRa
extern long last_packet_millis;
extern int last_packet_rssi;
extern float last_packet_snr;

extern float last_lat;
extern float last_lng;

void screen_init();
void screen_loop();
void screen_update();

void bt_init();
void bt_notify(uint8_t *data, size_t len);

void lora_init();
void lora_loop();
void lora_send(uint8_t *data, size_t len);

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
