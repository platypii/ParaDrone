package ws.baseline.autopilot.bluetooth;

import ws.baseline.autopilot.bluetooth.blessed.BluetoothCentral;
import ws.baseline.autopilot.bluetooth.blessed.BluetoothCentralCallback;
import ws.baseline.autopilot.bluetooth.blessed.BluetoothPeripheral;
import ws.baseline.autopilot.bluetooth.blessed.BluetoothPeripheralCallback;
import ws.baseline.autopilot.geo.LandingZone;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;
import timber.log.Timber;

import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_SEARCHING;
import static ws.baseline.autopilot.bluetooth.Util.byteArrayToHex;
import static android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_HIGH;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_CONNECTED;
import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_STOPPING;

/**
 * Thread that reads from bluetooth connection.
 * Autopilot messages are emitted as events.
 */
class BluetoothHandler {

    // Autopilot IDs
    private static final UUID apServiceId = UUID.fromString("ba5e0001-c55f-496f-a444-9855f5f14901");
    private static final UUID characteristicLocationId = UUID.fromString("ba5e0002-9235-47c8-b2f3-916cee33d802");
    private static final UUID characteristicLzId = UUID.fromString("ba5e0003-ed55-43fa-bb54-8e721e092603");
    private static final UUID characteristicCtrlId = UUID.fromString("ba5e0004-be98-4de9-9e9a-080b5bb41404");

    // Relay IDs
    private static final UUID relayServiceId = UUID.fromString("ba5e0005-228f-4d36-b9fa-8b99e1672005");
    private static final UUID relayCharacteristicId = UUID.fromString("ba5e0006-a35e-42fc-87e3-3775cc158906");

    private final Handler handler = new Handler();
    private final BluetoothService service;
    private final BluetoothCentral central;
    private BluetoothPeripheral peripheral;

    private boolean connected_ap = false;
    private boolean connected_relay = false;

    BluetoothHandler(@NonNull BluetoothService service, @NonNull Context context) {
        this.service = service;
        central = new BluetoothCentral(context, bluetoothCentralCallback, new Handler());
    }

    public void start() {
        // Scan for peripherals with a certain service UUIDs
        central.startPairingPopupHack();
        central.scanForPeripheralsWithServices(new UUID[]{apServiceId, relayServiceId});
    }

    // Callback for peripherals
    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(BluetoothPeripheral peripheral) {
            Timber.i("Bluetooth services discovered");

            // Request a new connection priority
            peripheral.requestConnectionPriority(CONNECTION_PRIORITY_HIGH);

            // Turn on notifications for AutoPilot Service
            if (peripheral.getService(apServiceId) != null) {
                peripheral.setNotify(peripheral.getCharacteristic(apServiceId, characteristicLocationId), true);
            }
            // Turn on notifications for LoRa Relay Service
            if (peripheral.getService(relayServiceId) != null) {
                peripheral.setNotify(peripheral.getCharacteristic(relayServiceId, relayCharacteristicId), true);
            }

            fetchLandingZone();
        }

        @Override
        public void onNotificationStateUpdate(BluetoothPeripheral peripheral, BluetoothGattCharacteristic characteristic, int status) {
            if ( status == GATT_SUCCESS) {
                if (peripheral.isNotifying(characteristic)) {
                    Timber.i("SUCCESS: Notify set to 'on' for %s", characteristic.getUuid());
                } else {
                    Timber.i("SUCCESS: Notify set to 'off' for %s", characteristic.getUuid());
                }
            } else {
                Timber.e("ERROR: Changing notification state failed for %s", characteristic.getUuid());
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothPeripheral peripheral, byte[] value, BluetoothGattCharacteristic characteristic, int status) {
            if ( status == GATT_SUCCESS) {
                Timber.i("SUCCESS: Writing <%s> to <%s>", byteArrayToHex(value), characteristic.getUuid().toString());
            } else {
                Timber.i("ERROR: Failed writing <%s> to <%s>", byteArrayToHex(value), characteristic.getUuid().toString());
            }
        }

        @Override
        public void onCharacteristicUpdate(BluetoothPeripheral peripheral, byte[] value, BluetoothGattCharacteristic characteristic, int status) {
            if (status != GATT_SUCCESS) return;
            if (value.length == 0) return;
            final UUID characteristicUUID = characteristic.getUuid();

            if (characteristicUUID.equals(characteristicLocationId)) {
                processBytes(value);
            } else if (characteristicUUID.equals(characteristicLzId)) {
                processBytes(value);
            }
        }
    };

    // Callback for central
    private final BluetoothCentralCallback bluetoothCentralCallback = new BluetoothCentralCallback() {

        @Override
        public void onConnectedPeripheral(BluetoothPeripheral connectedPeripheral) {
            peripheral = connectedPeripheral;
            Timber.i("Connected to '%s'", peripheral.getName());
            if (peripheral.getService(apServiceId) != null) {
                connected_ap = true;
            }
            if (peripheral.getService(relayServiceId) != null) {
                connected_relay = true;
            }
            service.setState(BT_CONNECTED);
        }

        @Override
        public void onConnectionFailed(BluetoothPeripheral peripheral, final int status) {
            Timber.e("Autopilot connection '%s' failed with status %d", peripheral.getName(), status);
        }

        @Override
        public void onDisconnectedPeripheral(final BluetoothPeripheral peripheral, final int status) {
            Timber.i("Autopilot disconnected '%s' with status %d", peripheral.getName(), status);

            // We were connected, and we're not stopping
            if (service.getState() == BT_CONNECTED) {
                service.setState(BT_SEARCHING);
                // Reconnect to this device when it becomes available again
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        central.autoConnectPeripheral(peripheral, peripheralCallback);
                    }
                }, 5000);
            }
        }

        @Override
        public void onDiscoveredPeripheral(BluetoothPeripheral peripheral, ScanResult scanResult) {
            Timber.i("Autopilot device found, connecting to: %s %s", peripheral.getName(), peripheral.getAddress());
            central.stopScan();
            central.connectPeripheral(peripheral, peripheralCallback);
        }

        @Override
        public void onBluetoothAdapterStateChanged(int state) {
            Timber.i("bluetooth adapter changed state to %d", state);
            if (state == BluetoothAdapter.STATE_ON) {
                // Bluetooth is on now, start scanning again
                // Scan for peripherals with a certain service UUIDs
                central.startPairingPopupHack();
                central.scanForPeripheralsWithServices(new UUID[]{apServiceId});
            }
        }
    };

    private void processBytes(@NonNull byte[] value) {
        if (value[0] == 'L' && value.length == 19) {
            // 'L', millis, lat, lng, alt
            final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
            final long millis = buf.getLong(1);
            final double lat = buf.getInt(9) * 1e-6; // microdegrees
            final double lng = buf.getInt(13) * 1e-6; // microdegrees
            final double alt = buf.getShort(17) * 0.1; // decimeters
            Timber.d("ap -> phone: location " + lat + " " + lng + " " + alt);
            APLocationMsg.update(millis, lat, lng, alt);
        } else if (value[0] == 'S' && value.length == 15) {
            // 'S', millis, vN, vE, climb
            final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
            final long millis = buf.getLong(1);
            final double vD = buf.getShort(9) * 0.01; // cm/s
            final double vE = buf.getShort(11) * 0.01; // cm/s
            final double climb = buf.getShort(13) * 0.01; // cm/s
            Timber.d("ap -> phone: speed " + vD + " " + vE + " " + climb);
            APSpeedMsg.update(millis, vD, vE, climb);
        } else if (value[0] == 'Z' && value.length == 13) {
            // 'Z', lat, lng, alt, dir
            final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
            final double lat = buf.getInt(1) * 1e-6; // microdegrees
            final double lng = buf.getInt(5) * 1e-6; // microdegrees
            final double alt = buf.getShort(9) * 0.1; // decimeters
            final double dir = buf.getShort(11) * 0.001; // milliradians
            final LandingZone lz = new LandingZone(lat, lng, alt, dir);
            Timber.i("ap -> phone: lz %s", lz);
            APLandingZone.update(lz, false);
        } else {
            Timber.e("ap -> phone: unknown %s", byteArrayToHex(value));
        }
    }

    void setLandingZone(LandingZone lz) {
        Timber.i("phone -> ap: set lz %s", lz);
        if (peripheral != null) {
            final BluetoothGattCharacteristic ch = peripheral.getCharacteristic(apServiceId, characteristicLzId);
            if (ch != null) {
                // Pack LZ into bytes
                final byte[] value = new byte[13];
                value[0] = 'Z';
                final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
                buf.putInt(1, (int)(lz.destination.lat * 1e6)); // microdegrees
                buf.putInt(5, (int)(lz.destination.lng * 1e6)); // microdegrees
                buf.putShort(9, (short)(lz.destination.alt * 10)); // decimeters
                buf.putShort(11, (short)(lz.landingDirection * 1000)); // milliradians
                if (!peripheral.writeCharacteristic(ch, value, WRITE_TYPE_DEFAULT)) {
                    Timber.e("Failed to set lz");
                }
//                fetchLandingZone();
            }
        }
    }

    void setControls(byte left, byte right) {
        Timber.i("phone -> ap: set controls %d %d", left, right);
        if (peripheral != null) {
            BluetoothGattCharacteristic ch = null;
            if (connected_ap) {
                ch = peripheral.getCharacteristic(apServiceId, characteristicCtrlId);
            } else if (connected_relay) {
                ch = peripheral.getCharacteristic(relayServiceId, relayCharacteristicId);
            }
            if (ch != null) {
                // Pack controls into bytes
                final byte[] value = new byte[3];
                value[0] = 'C';
                value[1] = left;
                value[2] = right;
                if (!peripheral.writeCharacteristic(ch, value, WRITE_TYPE_DEFAULT)) {
                    Timber.e("Failed to set controls");
                }
            }
        }
    }

    private void fetchLandingZone() {
        Timber.i("phone -> ap: fetch lz");
        if (peripheral != null) {
            final BluetoothGattCharacteristic ch = peripheral.getCharacteristic(apServiceId, characteristicLzId);
            if (ch != null) {
                if (!peripheral.readCharacteristic(ch)) {
                    Timber.e("Failed to fetch lz");
                }
            }
        }
    }

    void stop() {
        // Stop scanning
        central.stopScan();
        if (peripheral != null) {
            peripheral.cancelConnection();
        }
//        central.close();
        service.setState(BT_STOPPING);
    }

}
