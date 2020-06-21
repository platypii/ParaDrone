#include <heltec.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include "messages.h"
#include "paradrone.h"

#define AP_SERVICE        "ba5e0001-c55f-496f-a444-9855f5f14901"
#define AP_CHARACTERISTIC "ba5e0002-9235-47c8-b2f3-916cee33d802"

// Global bluetooth state
bool bt_connected = false;
static BLECharacteristic *ap_ch;

static void bt_send_lz();

class AutopilotServer : public BLEServerCallbacks {
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

class AutopilotCharacteristic : public BLECharacteristicCallbacks {
    void onRead(BLECharacteristic *pCharacteristic) {
    };
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();
      if (value[0] == 'C' && value.length() == 3) {
        // Serial.printf("BT ctrl %d %d\n", value[1], value[2]);
        rc_set_position(value[1], value[2]);
      } else if (value[0] == 'M' && value.length() == 3) {
        // Message is -127..127, speeds are -255..255
        const short left = ((short)(int8_t) value[1]) * 2;
        const short right = ((short)(int8_t) value[2]) * 2;
        Serial.printf("BT %.1fs motor %d %d\n", millis() * 1e-3, left, right);
        rc_set_speed(left, right);
      } else if (value[0] == 'F' && value.length() == 5) {
        const int freq = *(int*)(value.c_str() + 1);
        Serial.printf("BT %.1fs set lora freq %f\n", millis() * 1e-3, freq * 1e-6);
        LoRa.setFrequency(freq);
      } else if (value[0] == 'Q' && value.length() == 1) {
        // Send LZ in response
        bt_send_lz();
      } else if (value[0] == 'Z' && value.length() == 13) {
        set_landing_zone(value.c_str());
        screen_update();
      } else {
        Serial.printf("Unexpected BT msg %02x %d\n", value[0], value.length());
      }
    }
};

void bt_init() {
  // Init BLE
  BLEDevice::init("ParaDrone"); // Device name
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new AutopilotServer());
  BLEService *pService = pServer->createService(AP_SERVICE);

  // Autopilot characteristic
  ap_ch = pService->createCharacteristic(
    AP_CHARACTERISTIC,
    BLECharacteristic::PROPERTY_WRITE |
    BLECharacteristic::PROPERTY_NOTIFY
  );
  BLEDescriptor *pDescriptor = new BLEDescriptor(BLEUUID((uint16_t)0x2902));
  ap_ch->addDescriptor(pDescriptor);
  ap_ch->setCallbacks(new AutopilotCharacteristic());

  pService->start();
  // BLEAdvertising *pAdvertising = pServer->getAdvertising();  // this still is working for backward compatibility
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(AP_SERVICE);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
}

void bt_send_location(GeoPointV *point) {
  // Pack point into location message
  SpeedMessage msg = {
    'S',
    (int)(point->lat * 1e6), // microdegrees
    (int)(point->lng * 1e6), // microdegrees
    (short)(point->alt * 10), // decimeters
    (short)(point->vN * 0.01), // cm/s
    (short)(point->vE * 0.01), // cm/s
    (short)(point->climb * 0.01) // cm/s
  };
  uint8_t *data = (uint8_t*) &msg;
  size_t len = sizeof(msg);
  ap_ch->setValue(data, len);
  ap_ch->notify();
}

static void bt_send_lz() {
  LandingZoneMessage msg = pack_lz(current_landing_zone);
  uint8_t *data = (uint8_t*) &msg;
  size_t len = sizeof(msg);
  ap_ch->setValue(data, len);
  ap_ch->notify();
}
