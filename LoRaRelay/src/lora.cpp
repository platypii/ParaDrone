#include "heltec.h"
#include "relay.h"

#define PING_INTERVAL 30000

long last_packet_millis;
int last_packet_rssi;
float last_packet_snr;

// How long since last ping sent by relay
long last_ping_millis;

double last_lat = 0;
double last_lng = 0;
float last_alt = 0;

static void lora_send_ping();
static void lora_read();
static void read_location(uint8_t *buffer);
static void on_receive(int packet_size);
static size_t bytes_ready = 0; // Are bytes ready to be read?

void lora_init() {
  if (!LoRa.begin(LORA_BAND, true)) {
    Serial.println("LoRa init failed");
  }
  // LoRa.setPreambleLength();
  // LoRa.setSignalBandwidth(125E3); // 250E3, 125E3*, 62.5E3, ...
  // LoRa.setSPIFrequency(); // 1e6, 4e6, 8e6
  // LoRa.setSpreadingFactor(9); // 7..12 lower = more chirp/s = faster data, higher = better sensitivity. Default 11
  // LoRa.setTxPower(20, ); // Default 14
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
  Serial.printf("LoRa %.1fs sent %c size %d\n", millis() * 1e-3, data[0], len);
}

static void lora_send_ping() {
  last_ping_millis = millis();
  uint8_t data = 'P';
  lora_send(&data, 1);
}

static void lora_read() {
  uint8_t buffer[20];
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
  } else if (buffer[0] == 'Z' && buffer_len == 13) {
    Serial.printf("LoRa %.1fs LZ\n", millis() * 1e-3);
  } else if (buffer[0] == 'N' && buffer_len == 1) {
    Serial.printf("LoRa %.1fs No LZ\n", millis() * 1e-3);
  } else {
    Serial.printf("LoRa %.1fs unknown packet: ", millis() * 1e-3);
    for (int i = 0; i < buffer_len; i++) {
      Serial.printf("%02x", buffer[i]);
    }
    Serial.printf("\n");
  }
  screen_update();
}

static void read_location(uint8_t *buffer) {
  // Parse location
  LocationMessage *msg = (LocationMessage*) buffer;
  last_lat = msg->lat * 1e-6; // microdegrees
  last_lng = msg->lng * 1e-6; // microdegrees
  last_alt = msg->alt * 0.1f; // decimeters
  Serial.printf("LoRa %.1fs loc %f, %f, %.1f\n", millis() * 1e-3, last_lat, last_lng, last_alt);
}

static void on_receive(int packet_size) {
  // Don't do any real work here inside ISR
  bytes_ready = packet_size;
}
