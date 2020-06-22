package ws.baseline.paradrone.bluetooth;

import ws.baseline.paradrone.bluetooth.blessed.BluetoothCentral;
import ws.baseline.paradrone.bluetooth.blessed.BluetoothCentralCallback;
import ws.baseline.paradrone.bluetooth.blessed.BluetoothPeripheral;
import ws.baseline.paradrone.bluetooth.blessed.BluetoothPeripheralCallback;
import ws.baseline.paradrone.geo.LandingZone;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;
import timber.log.Timber;

import static android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_HIGH;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
import static ws.baseline.paradrone.bluetooth.BluetoothPreferences.DeviceMode.AP;
import static ws.baseline.paradrone.bluetooth.BluetoothPreferences.DeviceMode.RELAY;
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

    // Autopilot IDs
    private static final UUID apServiceId = UUID.fromString("ba5e0001-c55f-496f-a444-9855f5f14901");
    private static final UUID apCharacteristicId = UUID.fromString("ba5e0002-9235-47c8-b2f3-916cee33d802");

    // Relay IDs
    private static final UUID relayServiceId = UUID.fromString("ba5e0003-ed55-43fa-bb54-8e721e092603");
    private static final UUID relayCharacteristicId = UUID.fromString("ba5e0004-be98-4de9-9e9a-080b5bb41404");

    private final Handler handler = new Handler();
    private final BluetoothService service;
    private final BluetoothCentral central;
    private BluetoothPeripheral currentPeripheral;

    boolean connected_ap = false;
    boolean connected_relay = false;

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
        if (service.deviceMode == RELAY) {
            Timber.i("Scanning for relay");
            central.scanForPeripheralsWithServices(new UUID[]{relayServiceId});
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
                fetchLandingZone();
            }
            // Turn on notifications for LoRa Relay Service
            if (service.deviceMode == RELAY && peripheral.getService(relayServiceId) != null) {
                Timber.d("Enabling notifications for relay service");
                peripheral.setNotify(peripheral.getCharacteristic(relayServiceId, relayCharacteristicId), true);
                fetchLandingZone();
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
            } else if (characteristicUUID.equals(relayCharacteristicId)) {
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
            } else if (connectedPeripheral.getService(relayServiceId) != null) {
                connected_relay = true;
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
            } else if (connected_relay && service.deviceMode == RELAY) {
                Timber.d("Auto reconnecting to relay");
                connected_relay = false;
                if (BluetoothState.started(service.getState())) {
                    autoreconnect();
                }
            } else {
                Timber.d("Back to searching");
                connected_ap = false;
                connected_relay = false;
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
            } else if (service.deviceMode == RELAY && peripheral.getName().equals("ParaDroneRelay")) {
                Timber.i("Relay device found, connecting to: %s %s", peripheral.getName(), peripheral.getAddress());
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
            APLandingZone.parse(value);
        } else if (value[0] == 'N' && value.length == 1) {
            // No landing zone message
            APLandingZone.parse(value);
        } else {
            Timber.e("ap -> phone: unknown %s", byteArrayToHex(value));
        }
    }

    void setLandingZone(@NonNull LandingZone lz) {
        Timber.i("phone -> ap: set lz %s", lz);
        BluetoothGattCharacteristic ch = getCharacteristic();
        if (ch != null) {
            // Pack LZ into bytes
            final byte[] value = new byte[13];
            value[0] = 'Z';
            final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
            buf.putInt(1, (int)(lz.destination.lat * 1e6)); // microdegrees
            buf.putInt(5, (int)(lz.destination.lng * 1e6)); // microdegrees
            buf.putShort(9, (short)(lz.destination.alt * 10)); // decimeters
            buf.putShort(11, (short)(lz.landingDirection * 1000)); // milliradians
            if (!currentPeripheral.writeCharacteristic(ch, value, WRITE_TYPE_DEFAULT)) {
                Timber.e("Failed to set lz");
            }
            fetchLandingZone();
        } else {
            Timber.e("Failed to get characteristic");
        }
    }

    void setControls(byte left, byte right) {
        Timber.i("phone -> ap: set controls %d %d", left & 0xff, right & 0xff);
        BluetoothGattCharacteristic ch = getCharacteristic();
        if (ch != null) {
            // Pack controls into bytes
            final byte[] value = new byte[3];
            value[0] = 'C';
            value[1] = left;
            value[2] = right;
            if (!currentPeripheral.writeCharacteristic(ch, value, WRITE_TYPE_DEFAULT)) {
                Timber.e("Failed to set controls");
            }
        } else if (service.getState() == BT_CONNECTED) {
            Timber.e("Failed to get characteristic");
        }
    }

    void fetchLandingZone() {
        Timber.i("phone -> ap: fetch lz");
        BluetoothGattCharacteristic ch = getCharacteristic();
        if (ch != null) {
            // Request LZ from device
            final byte[] value = {'Q'};
            if (!currentPeripheral.writeCharacteristic(ch, value, WRITE_TYPE_DEFAULT)) {
                Timber.e("Failed to request lz");
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
            } else if (connected_relay) {
                return currentPeripheral.getCharacteristic(relayServiceId, relayCharacteristicId);
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
//        central.close();
        service.setState(BT_STOPPED);
    }

}
