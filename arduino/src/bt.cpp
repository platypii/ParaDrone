#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include "geo.h"
#include "paradrone.h"

#define SERVICE_PARADRONE       "ba5e0001-c55f-496f-a444-9855f5f14901"
#define CHARACTERISTIC_LOCATION "ba5e0002-9235-47c8-b2f3-916cee33d802"
#define CHARACTERISTIC_LZ       "ba5e0003-ed55-43fa-bb54-8e721e092603"

// Global bluetooth state
bool bt_connected = false;
static BLECharacteristic *ch_location;
static BLECharacteristic *ch_lz;

#pragma pack(1)
struct LocationMessage {
  char msg_type; // 'L'
  long long millis; // ms since epoch
  int lat; // microdegrees
  int lng; // microdegrees
  short alt; // decimeters
};

#pragma pack(1)
struct SpeedMessage {
  char msg_type; // 'S'
  long long millis; // ms since epoch
  short vN; // cm/s
  short vE; // cm/s
  short climb; // cm/s
};

class AutoPilotServer : public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      bt_connected = true;
      screen_update();
      Serial.println("BT client connected");
    };
    void onDisconnect(BLEServer* pServer) {
      bt_connected = false;
      screen_update();
      Serial.println("BT client disconnected");
    }
};

class LandingZoneCharacteristic : public BLECharacteristicCallbacks {
    void onRead(BLECharacteristic *pCharacteristic) {
    };
    void onWrite(BLECharacteristic *pCharacteristic) {
      const char *value = pCharacteristic->getValue().c_str();
      if (value[0] == 'Z') {
        set_landing_zone(value);
      } else {
        Serial.printf("Unexpected LZ write %02x", value[0]);
      }
    }
};

void bt_init() {
  // Init BLE
  BLEDevice::init("ParaDrone"); // Device name
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new AutoPilotServer());
  BLEService *pService = pServer->createService(SERVICE_PARADRONE);

  // Characteristic location
  ch_location = pService->createCharacteristic(
    CHARACTERISTIC_LOCATION,
    BLECharacteristic::PROPERTY_READ |
    BLECharacteristic::PROPERTY_NOTIFY
  );
  BLEDescriptor *pDescriptor = new BLEDescriptor(BLEUUID((uint16_t)0x2902));
  ch_location->addDescriptor(pDescriptor);

  // Characteristic LZ
  ch_lz = pService->createCharacteristic(
    CHARACTERISTIC_LZ,
    BLECharacteristic::PROPERTY_READ |
    BLECharacteristic::PROPERTY_WRITE
  );
  ch_lz->setCallbacks(new LandingZoneCharacteristic());

  pService->start();
  // BLEAdvertising *pAdvertising = pServer->getAdvertising();  // this still is working for backward compatibility
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_PARADRONE);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
}

void bt_notify(GeoPointV *point) {
  // Pack point into location message
  LocationMessage msg = {
    'L',
    point->millis,
    (int)(point->lat * 1e6), // microdegrees
    (int)(point->lng * 1e6), // microdegrees
    (short)(point->alt * 10) // decimeters
  };
  uint8_t *data = (uint8_t*) &msg;
  size_t len = sizeof(msg);
  ch_location->setValue(data, len);
  ch_location->notify();

  // Speed
  SpeedMessage msg2 = {
    'S',
    point->millis,
    (short)(point->vN * 0.01), // cm/s
    (short)(point->vE * 0.01), // cm/s
    (short)(point->climb * 0.01) // cm/s
  };
  uint8_t *data2 = (uint8_t*) &msg2;
  size_t len2 = sizeof(msg2);
  ch_location->setValue(data2, len2);
  ch_location->notify();
}
