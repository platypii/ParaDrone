#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include "rc.h"

#define RELAY_SERVICE        "ba5e0003-ed55-43fa-bb54-8e721e092603"
#define RELAY_CHARACTERISTIC "ba5e0004-be98-4de9-9e9a-080b5bb41404"

// Global bluetooth state
bool bt_connected = false;
static BLECharacteristic *ch_relay;

static void advertise();

class RelayServer : public BLEServerCallbacks {
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

class RelayCharacteristic : public BLECharacteristicCallbacks {
    void onRead(BLECharacteristic *pCharacteristic) {
    };
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();
      if (value[0] == 'F' && value.length() == 5) {
        const int freq = *(int*)(value.c_str() + 1);
        Serial.printf("%.1fs bt set lora freq %f\n", millis() * 1e-3, freq * 1e-6);
        lora_set_frequency(freq);
      } else {
        // Forward via LoRa
        lora_send((uint8_t*) value.data(), value.length());
      }
    }
};

void bt_init() {
  // Init BLE
  BLEDevice::init("ParaDroneRC"); // Device name
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new RelayServer());
  BLEService *pService = pServer->createService(RELAY_SERVICE);

  // Characteristic relay
  ch_relay = pService->createCharacteristic(
    RELAY_CHARACTERISTIC,
    BLECharacteristic::PROPERTY_WRITE |
    BLECharacteristic::PROPERTY_NOTIFY
  );
  BLEDescriptor *pDescriptor = new BLEDescriptor(BLEUUID((uint16_t)0x2902));
  // Bluetooth Core Spec v4.1+:
  // The default value for the Client Characteristic Configuration descriptor value shall be 0x0000
  uint8_t descriptorValue[2] = {0, 0};
  pDescriptor->setValue(descriptorValue, 2);
  ch_relay->addDescriptor(pDescriptor);
  ch_relay->setCallbacks(new RelayCharacteristic());

  pService->start();
  advertise();
}

static void advertise() {
  // BLEAdvertising *pAdvertising = pServer->getAdvertising(); // this still is working for backward compatibility
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(RELAY_SERVICE);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06); // helps with iphone issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
}

void bt_notify(uint8_t *data, size_t len) {
  ch_relay->setValue(data, len);
  ch_relay->notify();
}
