#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include "dtypes.h"

#define SERVICE_PARADRONE       "00ba5e00-c55f-496f-a444-9855f5f14992"
#define CHARACTERISTIC_LOCATION "00b45300-9235-47c8-b2f3-916cee33d85c"
#define CHARACTERISTIC_LZ       "00845300-ed55-43fa-bb54-8e721e0926ee"

// Global bluetooth state
static bool _BLEClientConnected = false;
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
      _BLEClientConnected = true;
      Serial.println("BT client connected");
    };

    void onDisconnect(BLEServer* pServer) {
      _BLEClientConnected = false;
      Serial.println("BT client disconnected");
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
  // ch_lz->setValue("ParaDrone v1.0");

  pService->start();
  // BLEAdvertising *pAdvertising = pServer->getAdvertising();  // this still is working for backward compatibility
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_PARADRONE);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
}

void bt_notify(GeoPointV point) {
  // Pack point into location message
  LocationMessage msg = {
    'L',
    point.millis,
    (int)(point.lat * 1e6), // microdegrees
    (int)(point.lng * 1e6), // microdegrees
    (short)(point.alt * 10) // decimeters
  };
  uint8_t *data = (uint8_t*) &msg;
  size_t len = sizeof(msg);
  ch_location->setValue(data, len);
  ch_location->notify();

  // Speed
  SpeedMessage msg2 = {
    'S',
    point.millis,
    (short)(point.vN * 0.01), // cm/s
    (short)(point.vE * 0.01), // cm/s
    (short)(point.climb * 0.01) // cm/s
  };
  uint8_t *data2 = (uint8_t*) &msg2;
  size_t len2 = sizeof(msg2);
  ch_location->setValue(data2, len2);
  ch_location->notify();
}
