#include "heltec.h"
#include "relay.h"

long last_packet_millis;
int last_packet_rssi;
float last_packet_snr;

float last_lat = 0;
float last_lng = 0;

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
  // LoRa.setSPIFrequency();
  LoRa.setSpreadingFactor(9); // 7..12 lower = more chirp/s = faster data, higher = better sensitivity. Default 11
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
}

void lora_send(uint8_t *data, size_t len) {
  Serial.printf("Sending LoRa packet %c size %d\n", data[0], len);
  LoRa.beginPacket(); // Explicit header mode for variable size packets
  LoRa.write(data, len);
  LoRa.endPacket();
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
    // LZ
  } else if (buffer[0] == 'N' && buffer_len == 1) {
    // No LZ
  } else {
    Serial.printf("LoRa unknown packet: ");
    for (int i = 0; i < buffer_len; i++) {
      Serial.printf("%02x", buffer[i]);
    }
    Serial.printf("\n");
  }
  screen_update();
}

static void read_location(uint8_t *buffer) {
  // Parse location
  LocationMessage *msg = (LocationMessage *) buffer;
  last_lat = msg->lat * 1e-6;
  last_lng = msg->lng * 1e-6;
  Serial.printf("LoRa location %f, %f\n", last_lat, last_lng);
}

static void on_receive(int packet_size) {
  // Don't do any real work here inside ISR
  bytes_ready = packet_size;
}
