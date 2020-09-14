package ws.baseline.paradrone.bluetooth;

import ws.baseline.paradrone.bluetooth.blessed.BluetoothCentral;
import ws.baseline.paradrone.bluetooth.blessed.BluetoothCentralCallback;
import ws.baseline.paradrone.bluetooth.blessed.BluetoothPeripheral;
import ws.baseline.paradrone.bluetooth.blessed.BluetoothPeripheralCallback;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.UUID;
import timber.log.Timber;

import static android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_HIGH;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
import static ws.baseline.paradrone.bluetooth.BluetoothPreferences.DeviceMode.AP;
import static ws.baseline.paradrone.bluetooth.BluetoothPreferences.DeviceMode.RC;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_CONNECTED;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_CONNECTING;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_SEARCHING;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_STARTED;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_STOPPED;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_STOPPING;
import static ws.baseline.paradrone.bluetooth.Util.byteArrayToHex;

/**
 * Thread that reads from bluetooth connection.
 * Autopilot messages are emitted as events.
 */
class BluetoothHandler {

    // Autopilot service IDs
    private static final UUID apServiceId = UUID.fromString("ba5e0001-c55f-496f-a444-9855f5f14901");
    private static final UUID apCharacteristicId = UUID.fromString("ba5e0002-9235-47c8-b2f3-916cee33d802");

    // Remote control service IDs
    private static final UUID rcServiceId = UUID.fromString("ba5e0003-ed55-43fa-bb54-8e721e092603");
    private static final UUID rcCharacteristicId = UUID.fromString("ba5e0004-be98-4de9-9e9a-080b5bb41404");

    private final Handler handler = new Handler();
    private final BluetoothService service;
    private final BluetoothCentral central;
    private BluetoothPeripheral currentPeripheral;

    boolean connected_ap = false;
    boolean connected_rc = false;

    BluetoothHandler(@NonNull BluetoothService service, @NonNull Context context) {
        this.service = service;
        central = new BluetoothCentral(context, bluetoothCentralCallback, new Handler());
    }

    public void start() {
        if (service.getState() == BT_STARTED) {
            scan();
        } else if (service.getState() == BT_SEARCHING) {
            Timber.w("Already searching");
        } else if (service.getState() == BT_STOPPING || service.getState() != BT_STOPPED) {
            // Stopping or stopped, don't search
        }
    }

    private void scan() {
        service.setState(BT_SEARCHING);
        // Scan for peripherals with a certain service UUIDs
        central.startPairingPopupHack();
        if (service.deviceMode == RC) {
            Timber.i("Scanning for RC");
            central.scanForPeripheralsWithServices(new UUID[]{rcServiceId});
        } else {
            Timber.i("Scanning for AP");
            central.scanForPeripheralsWithServices(new UUID[]{apServiceId});
        }
    }

    // Callback for peripherals
    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(BluetoothPeripheral peripheral) {
            Timber.i("Bluetooth services discovered for '%s'", peripheral.getName());

            // Request a new connection priority
            peripheral.requestConnectionPriority(CONNECTION_PRIORITY_HIGH);

            // Turn on notifications for AutoPilot Service
            if (service.deviceMode == AP && peripheral.getService(apServiceId) != null) {
                Timber.d("Enabling notifications for autopilot service");
                peripheral.setNotify(peripheral.getCharacteristic(apServiceId, apCharacteristicId), true);
                service.actions.fetchLandingZone();
            }
            // Turn on notifications for LoRa R/C service
            if (service.deviceMode == RC && peripheral.getService(rcServiceId) != null) {
                Timber.d("Enabling notifications for RC service");
                peripheral.setNotify(peripheral.getCharacteristic(rcServiceId, rcCharacteristicId), true);
                service.actions.fetchLandingZone();
            }
        }

        @Override
        public void onNotificationStateUpdate(BluetoothPeripheral peripheral, BluetoothGattCharacteristic characteristic, int status) {
            if (status == GATT_SUCCESS) {
                if (peripheral.isNotifying(characteristic)) {
                    Timber.d("SUCCESS: Notify set to 'on' for %s", characteristic.getUuid());
                } else {
                    Timber.d("SUCCESS: Notify set to 'off' for %s", characteristic.getUuid());
                }
            } else {
                Timber.e("ERROR: Changing notification state failed for %s", characteristic.getUuid());
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothPeripheral peripheral, byte[] value, BluetoothGattCharacteristic characteristic, int status) {
            if (status == GATT_SUCCESS) {
//                Timber.d("SUCCESS: Writing <%s> to <%s>", byteArrayToHex(value), characteristic.getUuid().toString());
            } else {
                Timber.w("ERROR: Failed writing <%s> to <%s>", byteArrayToHex(value), characteristic.getUuid().toString());
            }
        }

        @Override
        public void onCharacteristicUpdate(BluetoothPeripheral peripheral, byte[] value, BluetoothGattCharacteristic characteristic, int status) {
            if (status != GATT_SUCCESS) return;
            if (value.length == 0) return;
            final UUID characteristicUUID = characteristic.getUuid();

//            Timber.d("onCharacteristicUpdate %s", characteristicUUID);
            if (characteristicUUID.equals(apCharacteristicId)) {
                processBytes(value);
            } else if (characteristicUUID.equals(rcCharacteristicId)) {
                processBytes(value);
            }
        }
    };

    // Callback for central
    private final BluetoothCentralCallback bluetoothCentralCallback = new BluetoothCentralCallback() {

        @Override
        public void onConnectedPeripheral(BluetoothPeripheral connectedPeripheral) {
            currentPeripheral = connectedPeripheral;
            Timber.i("Connected to '%s'", connectedPeripheral.getName());
            if (connectedPeripheral.getService(apServiceId) != null) {
                connected_ap = true;
            } else if (connectedPeripheral.getService(rcServiceId) != null) {
                connected_rc = true;
            } else {
                Timber.e("Connected to device with no service?");
            }
            service.setState(BT_CONNECTED);
        }

        @Override
        public void onConnectionFailed(BluetoothPeripheral peripheral, final int status) {
            Timber.e("Autopilot connection '%s' failed with status %d", peripheral.getName(), status);
            start(); // start over
        }

        @Override
        public void onDisconnectedPeripheral(final BluetoothPeripheral peripheral, final int status) {
            Timber.i("Autopilot disconnected '%s' with status %d", peripheral.getName(), status);
            if (connected_ap && service.deviceMode == AP) {
                Timber.d("Auto reconnecting to AP");
                connected_ap = false;
                if (BluetoothState.started(service.getState())) {
                    autoreconnect();
                }
            } else if (connected_rc && service.deviceMode == RC) {
                Timber.d("Auto reconnecting to RC");
                connected_rc = false;
                if (BluetoothState.started(service.getState())) {
                    autoreconnect();
                }
            } else {
                Timber.d("Back to searching");
                connected_ap = false;
                connected_rc = false;
                currentPeripheral = null;
                // Go back to searching
                if (BluetoothState.started(service.getState())) {
                    scan();
                }
            }
        }

        private void autoreconnect() {
            // Reconnect to this device when it becomes available again
            service.setState(BT_SEARCHING);
            handler.postDelayed(() -> central.autoConnectPeripheral(currentPeripheral, peripheralCallback), 5000);
        }

        @Override
        public void onDiscoveredPeripheral(BluetoothPeripheral peripheral, ScanResult scanResult) {
            if (service.getState() != BT_SEARCHING) {
                Timber.e("Invalid BT state: %s", BluetoothState.toString(service.getState()));
                // TODO: return?
            }
            if (service.deviceMode == AP && peripheral.getName().equals("ParaDrone")) {
                Timber.i("Autopilot device found, connecting to: %s %s", peripheral.getName(), peripheral.getAddress());
                service.setState(BT_CONNECTING);
                central.stopScan();
                central.connectPeripheral(peripheral, peripheralCallback);
            } else if (service.deviceMode == RC && peripheral.getName().equals("ParaDroneRC")) {
                Timber.i("ParaDroneRC device found, connecting to: %s %s", peripheral.getName(), peripheral.getAddress());
                service.setState(BT_CONNECTING);
                central.stopScan();
                central.connectPeripheral(peripheral, peripheralCallback);
            } else {
                Timber.i("Wrong device found %s in mode %s", peripheral.getName(), service.deviceMode);
            }
        }

        @Override
        public void onBluetoothAdapterStateChanged(int state) {
            Timber.i("bluetooth adapter changed state to %d", state);
            if (state == BluetoothAdapter.STATE_ON) {
                // Bluetooth is on now, start scanning again
                start();
            }
        }
    };

    private void processBytes(@NonNull byte[] value) {
        if (value[0] == 'L' && value.length == 11) {
            // Location message
            ApLocationMsg.parse(value);
        } else if (value[0] == 'S' && value.length == 17) {
            // Speed message
            ApSpeedMsg.parse(value);
        } else if (value[0] == 'Z' && value.length == 13) {
            // Landing zone message
            ApLandingZone.parse(value);
        } else {
            Timber.e("ap -> phone: unknown %c %s", (char) value[0], byteArrayToHex(value));
        }
    }

    void sendCommand(byte[] value) {
        Timber.d("phone -> ap: cmd %c", (char) value[0]);
        final BluetoothGattCharacteristic ch = getCharacteristic();
        if (ch != null) {
            if (!currentPeripheral.writeCharacteristic(ch, value, WRITE_TYPE_DEFAULT)) {
                Timber.e("Failed to send cmd %c", (char) value[0]);
            }
        } else {
            Timber.e("Failed to get characteristic");
        }
    }

    @Nullable
    private BluetoothGattCharacteristic getCharacteristic() {
        if (currentPeripheral != null) {
            if (connected_ap) {
                return currentPeripheral.getCharacteristic(apServiceId, apCharacteristicId);
            } else if (connected_rc) {
                return currentPeripheral.getCharacteristic(rcServiceId, rcCharacteristicId);
            }
        }
        return null;
    }

    /**
     * Terminate an existing connection (because we're switching devices)
     */
    void disconnect() {
        if (currentPeripheral != null) {
            // will receive callback in onDisconnectedPeripheral
            currentPeripheral.cancelConnection();
        } else if (service.getState() == BT_SEARCHING) {
            // Searching for other device, stop and restart search
            Timber.d("Restarting current scan");
            central.stopScan();
            service.setState(BT_STARTED);
            scan();
        }
    }

    void stop() {
        service.setState(BT_STOPPING);
        // Stop scanning
        central.stopScan();
        if (currentPeripheral != null) {
            currentPeripheral.cancelConnection();
        }
        // Don't close central because it won't come back if we re-start
//        central.close();
        service.setState(BT_STOPPED);
    }

}
