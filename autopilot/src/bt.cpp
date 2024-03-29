#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include "paradrone.h"

#define AP_SERVICE        "ba5e0001-c55f-496f-a444-9855f5f14901"
#define AP_CHARACTERISTIC "ba5e0002-9235-47c8-b2f3-916cee33d802"

// Global bluetooth state
bool bt_connected = false;
static BLECharacteristic *ap_ch;

static void advertise();
static void bt_send_lz();
static void bt_send_motor_config();

class AutopilotServer : public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      bt_connected = true;
      screen_update();
      Serial.printf("%.1fs bt client connected\n", millis() * 1e-3);
    };
    void onDisconnect(BLEServer* pServer) {
      bt_connected = false;
      screen_update();
      Serial.printf("%.1fs bt client disconnected\n", millis() * 1e-3);
      advertise();
    }
};

class AutopilotCharacteristic : public BLECharacteristicCallbacks {
    void onRead(BLECharacteristic *pCharacteristic) {
    };
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();
      if (value[0] == 'C' && value.length() == 8) {
        set_motor_config((MotorConfigMessage*) value.c_str());
      } else if (value[0] == 'D' && value.length() == 17) {
        // Location from bluetooth, used for replay
        GeoPointV *point = unpack_speed(millis(), (SpeedMessage*) value.c_str());
        update_location(point);
      } else if (value[0] == 'M' && value.length() == 2) {
        // Flight mode
        const uint8_t mode = value[1];
        if (mode == MODE_IDLE) {
          Serial.printf("%.1fs bt mode idle\n", millis() * 1e-3);
        } else if (mode == MODE_AUTO) {
          Serial.printf("%.1fs bt mode auto\n", millis() * 1e-3);
        } else {
          Serial.printf("%.1fs bt bad mode %d\n", millis() * 1e-3, mode);
        }
        set_flight_mode(mode);
      } else if (value[0] == 'Q' && value.length() == 2) {
        Serial.printf("%.1fs bt query %c\n", millis() * 1e-3, value[1]);
        if (value[1] == 'C') {
          // Send motor config in response
          bt_send_motor_config();
        } else if (value[1] == 'I') {
          // Run motor calibration
          calibrate();
        } else if (value[1] == 'Z') {
          // Send LZ in response
          bt_send_lz();
        }
      } else if (value[0] == 'S' && value.length() == 3) {
        // Message is -127..127, speeds are -255..255
        const short left = ((short)(int8_t) value[1]) * 2;
        const short right = ((short)(int8_t) value[2]) * 2;
        Serial.printf("%.1fs bt motor speed %d %d\n", millis() * 1e-3, left, right);
        rc_set_speed(left, right);
      } else if (value[0] == 'T' && value.length() == 3) {
        // Serial.printf("%.1fs bt toggle %d %d\n", millis() * 1e-3, value[1], value[2]);
        rc_set_position(value[1], value[2]);
      } else if (value[0] == 'W') {
        Serial.printf("%.1fs bt web server\n", millis() * 1e-3);
        const char *userpass = value.c_str();
        char *split = strchr(userpass, ':');
        if (split) {
          *split = 0;
          web_init(userpass + 1, split + 1);
        } else {
          Serial.printf("%.1fs bt invalid command %s\n", millis() * 1e-3, value.c_str());
        }
      } else if (value[0] == 'Z' && value.length() == 13) {
        set_landing_zone((LandingZoneMessage*) value.c_str());
        screen_update();
      } else {
        Serial.printf("%.1fs bt unexpected %02x size %d\n", millis() * 1e-3, value[0], value.length());
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
  // Bluetooth Core Spec v4.1+:
  // The default value for the Client Characteristic Configuration descriptor value shall be 0x0000
  uint8_t descriptorValue[2] = {0, 0};
  pDescriptor->setValue(descriptorValue, 2);
  ap_ch->addDescriptor(pDescriptor);
  ap_ch->setCallbacks(new AutopilotCharacteristic());

  pService->start();
  advertise();
}

static void advertise() {
  // BLEAdvertising *pAdvertising = pServer->getAdvertising(); // this still is working for backward compatibility
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(AP_SERVICE);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06); // helps with iphone issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
}

void bt_send_location(GeoPointV *point) {
  if (bt_connected) {
    // Pack point into location message
    SpeedMessage msg = pack_speed(point);
    uint8_t *data = (uint8_t*) &msg;
    size_t len = sizeof(msg);
    ap_ch->setValue(data, len);
    ap_ch->notify();
  }
}

static void bt_send_lz() {
  if (bt_connected) {
    if (config_landing_zone) {
      LandingZoneMessage msg = pack_lz(config_landing_zone);
      ap_ch->setValue((uint8_t*) &msg, sizeof(msg));
    } else {
      ap_ch->setValue((uint8_t*) "Z", 1);
    }
    ap_ch->notify();
  }
}

static void bt_send_motor_config() {
  if (bt_connected) {
    uint8_t *data = (uint8_t*) &motor_config;
    size_t len = sizeof(motor_config);
    ap_ch->setValue(data, len);
    ap_ch->notify();
  }
}

void bt_send_url(const char *url) {
  if (bt_connected) {
    const size_t len = strnlen(url, 19);
    uint8_t data[20] = "U";
    memcpy(data + 1, url, len);
    ap_ch->setValue(data, len + 1);
    ap_ch->notify();
  }
}

void bt_send_calibration(uint16_t left1, uint16_t left2, uint16_t right1, uint16_t right2) {
  if (bt_connected) {
    uint8_t data[9] = "I";
    *(short*)(data + 1) = left1;
    *(short*)(data + 3) = left2;
    *(short*)(data + 5) = right1;
    *(short*)(data + 7) = right2;
    ap_ch->setValue(data, 9);
    ap_ch->notify();
  }
}
