#include <heltec.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include "rc.h"

#define RELAY_SERVICE        "ba5e0003-ed55-43fa-bb54-8e721e092603"
#define RELAY_CHARACTERISTIC "ba5e0004-be98-4de9-9e9a-080b5bb41404"

// Global bluetooth state
bool bt_connected = false;
static BLECharacteristic *ch_relay;

class RelayServer : public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      bt_connected = true;
      Serial.println("BT client connected");
      screen_update();
    };
    void onDisconnect(BLEServer* pServer) {
      bt_connected = false;
      Serial.println("BT client disconnected");
      screen_update();
    }
};

class RelayCharacteristic : public BLECharacteristicCallbacks {
    void onRead(BLECharacteristic *pCharacteristic) {
    };
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();
      if (value[0] == 'F' && value.length() == 5) {
        const int freq = *(int*)(value.c_str() + 1);
        Serial.printf("LoRa %.1fs set freq %f\n", millis() * 1e-3, freq * 1e-6);
        LoRa.setFrequency(freq);
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
  ch_relay->addDescriptor(pDescriptor);
  ch_relay->setCallbacks(new RelayCharacteristic());

  pService->start();
  // BLEAdvertising *pAdvertising = pServer->getAdvertising();  // this still is working for backward compatibility
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(RELAY_SERVICE);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
}

void bt_notify(uint8_t *data, size_t len) {
  ch_relay->setValue(data, len);
  ch_relay->notify();
}
