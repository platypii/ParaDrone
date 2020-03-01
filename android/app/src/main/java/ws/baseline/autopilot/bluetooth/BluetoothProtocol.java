package ws.baseline.autopilot.bluetooth;

import java.util.UUID;

/**
 * Generic bluetooth LE two-way protocol handler
 */
interface BluetoothProtocol {

    void onServicesDiscovered();

    void processBytes(byte[] value);

    UUID getCharacteristic();

}
