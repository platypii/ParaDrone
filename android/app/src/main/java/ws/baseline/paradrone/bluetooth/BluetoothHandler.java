package ws.baseline.paradrone.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.ConnectionPriority;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;
import com.welie.blessed.WriteType;
import java.util.UUID;
import timber.log.Timber;
import ws.baseline.paradrone.Permissions;

import static com.welie.blessed.BluetoothBytesParser.asHexString;
import static ws.baseline.paradrone.bluetooth.BluetoothPreferences.DeviceMode.AP;
import static ws.baseline.paradrone.bluetooth.BluetoothPreferences.DeviceMode.RC;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_CONNECTED;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_CONNECTING;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_SEARCHING;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_STARTED;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_STOPPED;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_STOPPING;

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

    private final Activity activity;
    private final Handler handler = new Handler();
    @NonNull
    private final BluetoothService service;
    @NonNull
    private final BluetoothCentralManager central;
    @Nullable
    private BluetoothPeripheral currentPeripheral;
    @Nullable
    private BluetoothGattCharacteristic currentCharacteristic;

    boolean connected_ap = false;
    boolean connected_rc = false;

    BluetoothHandler(@NonNull BluetoothService service, @NonNull Activity activity) {
        this.activity = activity;
        this.service = service;
        central = new BluetoothCentralManager(activity, bluetoothCentralManagerCallback, new Handler());
    }

    public void start() {
        if (service.getState() == BT_STARTED) {
            scanIfPermitted();
        } else if (service.getState() == BT_SEARCHING) {
            Timber.w("Already searching");
        } else if (service.getState() == BT_STOPPING || service.getState() != BT_STOPPED) {
            Timber.w("Already stopping");
        }
    }

    /**
     * Check if bluetooth permissions are granted, and then scan().
     */
    private void scanIfPermitted() {
        if (Permissions.hasBluetoothPermissions(activity)) {
            Timber.d("Bluetooth permitted, starting scan");
            try {
                scan();
            } catch (SecurityException e) {
                Timber.e(e, "Permission exception while bluetooth scanning");
            }
        } else {
            Timber.w("Bluetooth permission required");
            service.setState(BT_STOPPED);
            Permissions.requestBluetoothPermissions(activity);
        }
    }

    /**
     * Scan for bluetooth peripherals
     */
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
        public void onServicesDiscovered(@NonNull BluetoothPeripheral peripheral) {
            Timber.i("Bluetooth services discovered for '%s'", peripheral.getName());

            // Request a higher MTU, iOS always asks for 185
            peripheral.requestMtu(185);

            // Request a new connection priority
            peripheral.requestConnectionPriority(ConnectionPriority.HIGH);

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
        public void onNotificationStateUpdate(@NonNull final BluetoothPeripheral peripheral, @NonNull final BluetoothGattCharacteristic characteristic, @NonNull final GattStatus status) {
            if (status == GattStatus.SUCCESS) {
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
        public void onCharacteristicWrite(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value, @NonNull BluetoothGattCharacteristic characteristic, @NonNull final GattStatus status) {
            if (status == GattStatus.SUCCESS) {
//                Timber.d("SUCCESS: Writing <%s> to <%s>", byteArrayToHex(value), characteristic.getUuid().toString());
            } else {
                Timber.w("ERROR: Failed writing <%s> to <%s>", asHexString(value, "-"), characteristic.getUuid().toString());
            }
        }

        @Override
        public void onCharacteristicUpdate(@NonNull BluetoothPeripheral peripheral, @NonNull byte[] value, @NonNull BluetoothGattCharacteristic characteristic, @NonNull final GattStatus status) {
            if (status != GattStatus.SUCCESS) return;
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
    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {

        @Override
        public void onConnectedPeripheral(@NonNull BluetoothPeripheral connectedPeripheral) {
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
        public void onConnectionFailed(@NonNull BluetoothPeripheral peripheral, @NonNull final HciStatus status) {
            Timber.e("Autopilot connection '%s' failed with status %s", peripheral.getName(), status);
            start(); // start over
        }

        @Override
        public void onDisconnectedPeripheral(@NonNull final BluetoothPeripheral peripheral, @NonNull final HciStatus status) {
            Timber.i("Autopilot disconnected '%s' with status %s", peripheral.getName(), status);
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
        public void onDiscoveredPeripheral(@NonNull BluetoothPeripheral peripheral, @NonNull ScanResult scanResult) {
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

    /**
     * Parse bluetooth messages from the device.
     * @param value the message to parse
     */
    private void processBytes(@NonNull byte[] value) {
        if (value[0] == 'C' && value.length == 8) {
            // Config message
            ApConfigMsg.parse(value);
        } else if (value[0] == 'D' && value.length == 17) {
            // Speed message
            ApSpeedMsg.parse(value);
        } else if (value[0] == 'I' && value.length == 9) {
            // Calibration message
            ApCalibrationMsg.parse(value);
        } else if (value[0] == 'L' && value.length == 11) {
            // Location message
            ApLocationMsg.parse(value);
        } else if (value[0] == 'U') {
            // Web server url
            UrlMsg.parse(value);
        } else if (value[0] == 'Z' && value.length == 13) {
            // Landing zone message
            ApLandingZone.parse(value);
        } else {
            Timber.e("ap -> phone: unknown %c %s", (char) value[0], asHexString(value, "-"));
        }
    }

    /**
     * Send a command to the device.
     * @param value the command to send
     */
    void sendCommand(@NonNull byte[] value) {
        Timber.d("phone -> ap: cmd %c", (char) value[0]);
        final BluetoothGattCharacteristic ch = getCharacteristic();
        if (currentPeripheral != null && ch != null) {
            if (!currentPeripheral.writeCharacteristic(ch, value, WriteType.WITH_RESPONSE)) {
                Timber.e("Failed to send cmd %c", (char) value[0]);
            }
        } else {
            Timber.e("Failed to get characteristic");
        }
    }

    @Nullable
    private BluetoothGattCharacteristic getCharacteristic() {
        if (currentCharacteristic == null && currentPeripheral != null) {
            if (connected_ap) {
                currentCharacteristic = currentPeripheral.getCharacteristic(apServiceId, apCharacteristicId);
            } else if (connected_rc) {
                currentCharacteristic =  currentPeripheral.getCharacteristic(rcServiceId, rcCharacteristicId);
            }
        }
        return currentCharacteristic;
    }

    /**
     * Terminate an existing connection (because we're switching devices)
     */
    void disconnect() {
        currentCharacteristic = null;
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

    /**
     * Stop all bluetooth scanning and connections.
     */
    void stop() {
        currentCharacteristic = null;
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
