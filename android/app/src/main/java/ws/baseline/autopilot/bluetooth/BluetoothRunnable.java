package ws.baseline.autopilot.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_CONNECTED;
import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_CONNECTING;
import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_DISCONNECTED;
import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_STARTING;
import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_STOPPING;

/**
 * Thread that reads from bluetooth connection.
 * Autopilot messages are emitted as events.
 */
class BluetoothRunnable implements Runnable {
    private static final String TAG = "BluetoothRunnable";

    @NonNull
    private final BluetoothService service;
    @NonNull
    private final Context context;
    @NonNull
    private final BluetoothAdapter bluetoothAdapter;
    @Nullable
    private BluetoothGatt bluetoothGatt;
    @Nullable
    private BluetoothLeScanner bluetoothScanner;
    @Nullable
    private ScanCallback scanCallback;
    @Nullable
    private BluetoothProtocol protocol;

    BluetoothRunnable(@NonNull BluetoothService service, @NonNull Context context, @NonNull BluetoothAdapter bluetoothAdapter) {
        this.service = service;
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    /**
     * Scan for bluetooth LE devices that look like an autopilot device
     */
    @Override
    public void run() {
        Log.i(TAG, "Autopilot bluetooth thread starting");
        if (!bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled");
            return;
        }
        // Scan for autopilot devices
        scan();
    }

    /**
     * Scan BLE for autopilot devices
     */
    private void scan() {
        Log.i(TAG, "Scanning for autopilot");
        service.setState(BT_STARTING);
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothScanner == null) {
            Log.e(TAG, "Failed to get bluetooth LE scanner");
            return;
        }
        final ScanFilter scanFilter = new ScanFilter.Builder().build();
        final List<ScanFilter> scanFilters = Collections.singletonList(scanFilter);
        final ScanSettings scanSettings = new ScanSettings.Builder().build();
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                super.onScanResult(callbackType, result);
                if (service.getState() == BT_STARTING) {
                    final BluetoothDevice device = result.getDevice();
                    final ScanRecord record = result.getScanRecord();
                    if (AutopilotProtocol.isAutopilot(device, record)) {
                        Log.i(TAG, "Autopilot device found, connecting to: " + device.getName() + " " + device.getAddress());
                        connect(device);
                        protocol = new AutopilotProtocol(bluetoothGatt);
                    }
                }
            }
        };
        bluetoothScanner.startScan(scanFilters, scanSettings, scanCallback);
    }

    private void connect(@NonNull BluetoothDevice device) {
        stopScan();
        service.setState(BT_CONNECTING);
        // Connect to device
        bluetoothGatt = device.connectGatt(context, true, gattCallback);
        // Log event
        final Bundle bundle = new Bundle();
        bundle.putString("device_name", device.getName());
    }

    private void stopScan() {
        if (service.getState() != BT_STARTING) {
            Log.e(TAG, "Scanner shouldn't exist in state " + service.getState());
        }
        // Stop scanning
        if (bluetoothScanner != null) {
            bluetoothScanner.stopScan(scanCallback);
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Bluetooth profile connected");
                // TODO: Do we need to discover services? Or can we just connect?
                bluetoothGatt.discoverServices();
                service.setState(BT_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Autopilot disconnected");
                service.setState(BT_DISCONNECTED);
            } else {
                Log.i(TAG, "Autopilot state " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Bluetooth services discovered");
                protocol.onServicesDiscovered();
            } else {
                Log.i(TAG, "Bluetooth service discovery failed");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic ch) {
            if (ch.getUuid().equals(protocol.getCharacteristic())) {
                protocol.processBytes(ch.getValue());
            } else {
                Log.i(TAG, "Autopilot onCharacteristicChanged " + ch);
            }
        }
    };

    void stop() {
        // Close bluetooth socket
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        // Stop scanning
        if (service.getState() == BT_STARTING) {
            stopScan();
        }
        service.setState(BT_STOPPING);
    }

}
