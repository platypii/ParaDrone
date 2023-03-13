#include <heltec.h>
#include "rc.h"

#define MAX_PACKET_SIZE 20 // Same as BT
#define PING_INTERVAL 30000

long last_packet_millis;
int last_packet_rssi;
float last_packet_snr;

// How long since we last sent ping
long last_ping_millis = 5000 - PING_INTERVAL; // Initial ping 5 seconds after startup

double last_lat = 0;
double last_lng = 0;
float last_alt = NAN;
long last_fix_millis = -1;

static void lora_send_ping();
static void lora_read();
static void read_location(uint8_t *buffer);
static void on_receive(int packet_size);
static size_t bytes_ready = 0; // Are bytes ready to be read?

void lora_init() {
  if (!LoRa.begin(LORA_BAND, true)) {
    Serial.printf("%.1fs lora init failed\n", millis() * 1e-3);
  }
  // LoRa.setPreambleLength();
  // LoRa.setSignalBandwidth(125E3); // 250E3, 125E3*, 62.5E3, ...
  // LoRa.setSPIFrequency(); // 1e6, 4e6, 8e6
  LoRa.setSpreadingFactor(10); // 7..12 default 11. lower = more chirp/s = faster data, higher = better sensitivity
  // LoRa.setTxPower(20, RF_PACONFIG_PASELECT_PABOOST); // 5..20 default 14
  // LoRa.setTxPowerMax(20);
  LoRa.setCodingRate4(8); // 5..8
  LoRa.setSyncWord(0xBA);
  LoRa.enableCrc();
  LoRa.onReceive(on_receive);
  LoRa.receive();
}

void lora_loop() {
  if (bytes_ready) {
    lora_read();
    bytes_ready = 0;
  }

  // Send ping periodically
  if (millis() - last_ping_millis > PING_INTERVAL) {
    lora_send_ping();
  }
}

void lora_send(uint8_t *data, size_t len) {
  LoRa.beginPacket(); // Explicit header mode for variable size packets
  LoRa.write(data, len);
  LoRa.endPacket();
  LoRa.receive(); // Put it back in receive mode

  // Logging
  if (data[0] == 'M' && len == 2) {
    Serial.printf("%.1fs lora sent mode %d\n", millis() * 1e-3, data[1]);
  } else if (data[0] == 'P' && len == 1) {
    Serial.printf("%.1fs lora sent ping\n", millis() * 1e-3);
  } else if (data[0] == 'Q' && len == 2) {
    Serial.printf("%.1fs lora sent query %c\n", millis() * 1e-3, data[1]);
  } else if (data[0] == 'S' && len == 3) {
    Serial.printf("%.1fs lora sent speed %d %d\n", millis() * 1e-3, data[1], data[2]);
  } else if (data[0] == 'T' && len == 3) {
    Serial.printf("%.1fs lora sent toggle %d %d\n", millis() * 1e-3, data[1], data[2]);
  } else if (data[0] == 'Z' && len == 13) {
    Serial.printf("%.1fs lora sent lz\n", millis() * 1e-3);
  } else {
    Serial.printf("%.1fs lora sent %c size %d\n", millis() * 1e-3, data[0], len);
  }
}

void lora_set_frequency(long frequency) {
  LoRa.setFrequency(frequency);
}

static void lora_send_ping() {
  last_ping_millis = millis();
  uint8_t data = 'P';
  lora_send(&data, 1);
}

static void lora_read() {
  uint8_t buffer[MAX_PACKET_SIZE];
  int buffer_len = 0;
  LoRa.parsePacket();
  // Read bytes
  while (LoRa.available() && buffer_len < 20) {
    buffer[buffer_len++] = LoRa.read();
  }
  last_packet_millis = millis();
  last_packet_rssi = LoRa.packetRssi();
  last_packet_snr = LoRa.packetSnr();

  // Send to bluetooth
  bt_notify(buffer, buffer_len);

  if (buffer[0] == 'L' && buffer_len == 11) {
    // Parse location packet so we can display
    read_location(buffer);
  } else if (buffer[0] == 'P' && buffer_len == 1) {
    // Received ping, probably from another RC device
    // Serial.printf("%.1fs lora ping\n", millis() * 1e-3);
  } else if (buffer[0] == 'Z' && buffer_len == 13) {
    Serial.printf("%.1fs lora lz\n", millis() * 1e-3);
  } else if (buffer[0] == 'Z' && buffer_len == 1) {
    Serial.printf("%.1fs lora no lz\n", millis() * 1e-3);
  } else {
    Serial.printf("%.1fs lora unexpected %02x size %d\n", millis() * 1e-3, buffer[0], buffer_len);
    for (int i = 0; i < buffer_len; i++) {
      Serial.printf("%02x", buffer[i]);
    }
    Serial.print('\n');
  }
  screen_update();
}

static void read_location(uint8_t *buffer) {
  // Parse location
  LocationMessage *msg = (LocationMessage*) buffer;
  last_lat = msg->lat * 1e-6; // microdegrees
  last_lng = msg->lng * 1e-6; // microdegrees
  last_alt = msg->alt * 0.1f; // decimeters
  last_fix_millis = millis();
  Serial.printf("%.1fs lora loc %f, %f, %.1f\n", last_fix_millis * 1e-3, last_lat, last_lng, last_alt);
}

static void on_receive(int packet_size) {
  // Don't do any real work here inside ISR
  bytes_ready = packet_size;
}
