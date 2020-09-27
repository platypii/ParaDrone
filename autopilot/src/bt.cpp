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
static void bt_send_motor_config();

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
      if (value[0] == 'C' && value.length() == 10) {
        set_motor_config((MotorConfigMessage*) value.c_str());
      } else if (value[0] == 'M' && value.length() == 2) {
        const uint8_t mode = value[1];
        Serial.printf("BT %.1fs mode %d\n", millis() * 1e-3, mode);
        set_flight_mode(mode);
      } else if (value[0] == 'Q' && value.length() == 2) {
        Serial.printf("BT %.1fs Q %c\n", millis() * 1e-3, value[1]);
        if (value[1] == 'Z') {
          // Send LZ in response
          bt_send_lz();
        } else if (value[1] == 'C') {
          // Send motor config in response
          bt_send_motor_config();
        }
      } else if (value[0] == 'S' && value.length() == 3) {
        // Message is -127..127, speeds are -255..255
        const short left = ((short)(int8_t) value[1]) * 2;
        const short right = ((short)(int8_t) value[2]) * 2;
        Serial.printf("BT %.1fs motor speed %d %d\n", millis() * 1e-3, left, right);
        rc_set_speed(left, right);
      } else if (value[0] == 'T' && value.length() == 3) {
        // Serial.printf("BT toggle %d %d\n", value[1], value[2]);
        rc_set_position(value[1], value[2]);
      } else if (value[0] == 'Z' && value.length() == 13) {
        set_landing_zone((LandingZoneMessage*) value.c_str());
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
  LandingZoneMessage msg = pack_lz(config_landing_zone);
  uint8_t *data = (uint8_t*) &msg;
  size_t len = sizeof(msg);
  ap_ch->setValue(data, len);
  ap_ch->notify();
}

static void bt_send_motor_config() {
  uint8_t *data = (uint8_t*) &motor_config;
  size_t len = sizeof(motor_config);
  ap_ch->setValue(data, len);
  ap_ch->notify();
}
