package ws.baseline.autopilot.bluetooth;

import ws.baseline.autopilot.geo.LandingZone;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanRecord;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import java.util.UUID;

/**
 * Implements bluetooth two-way communication protocol with autopilot device.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class AutopilotProtocol implements BluetoothProtocol {
    private static final String TAG = "AutopilotProtocol";

    // Autopilot service UUID
    private static final UUID apService = UUID.fromString("00ba5e00-c55f-496f-a444-9855f5f14992");
    // Autopilot characteristic UUID
    private static final UUID apCharacteristic = UUID.fromString("00b45300-9235-47c8-b2f3-916cee33d85c");

    // Client Characteristic Configuration (what we subscribe to)
    private static final UUID clientCharacteristicDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // Messages from autopilot device
    private static final byte statusLocation = 0x20;
    // Messages to autopilot device
    private static final byte setLandingZone = 0x40;

    // Protocol state
    private final BluetoothGatt bluetoothGatt;

    AutopilotProtocol(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    static boolean isAutopilot(BluetoothDevice device, ScanRecord record) {
        final String deviceName = device.getName();
        return "ParaDrone".equals(deviceName);
    }


    @Override
    public void onServicesDiscovered() {
        requestAutopilotService();
    }

    @Override
    public void processBytes(@NonNull byte[] value) {
        // TODO: Buffer into lines?
        processSentence(value);
    }

    @Override
    public UUID getCharacteristic() {
        return apCharacteristic;
    }

    private void processSentence(@NonNull byte[] value) {
        if (value[0] == statusLocation) {
            Log.i(TAG, "ap -> phone: location");
        } else {
            Log.w(TAG, "ap -> phone: unknown " + Util.byteArrayToHex(value));
        }
    }

    private void requestAutopilotService() {
        final BluetoothGattService service = bluetoothGatt.getService(apService);
        final BluetoothGattCharacteristic ch = service.getCharacteristic(apCharacteristic);
        if (ch != null) {
            // Enables notification locally:
            bluetoothGatt.setCharacteristicNotification(ch, true);
            // Enables notification on the device
            final BluetoothGattDescriptor descriptor = ch.getDescriptor(clientCharacteristicDescriptor);
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor);
            } else {
                Log.e(TAG, "Null descriptor for " + clientCharacteristicDescriptor);
            }
        }
    }

    private void sendLandingZone(LandingZone lz) {
        Log.d(TAG, "app -> device: lz " + lz);
        final BluetoothGattService service = bluetoothGatt.getService(apService);
        final BluetoothGattCharacteristic ch = service.getCharacteristic(apCharacteristic);
        if (ch != null) {
            final byte[] value = {setLandingZone, 0x00};
            ch.setValue(value);
            bluetoothGatt.writeCharacteristic(ch);
        }
    }

}
