package ws.baseline.autopilot.bluetooth;

import ws.baseline.autopilot.geo.LandingZone;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
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

    // Rangefinder service
    private static final UUID rangefinderService = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    // Rangefinder characteristic
    private static final UUID rangefinderCharacteristic = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

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

    @Override
    public void onServicesDiscovered() {
        requestRangefinderService();
    }

    @Override
    public void processBytes(@NonNull byte[] value) {
        // TODO: Buffer into lines?
        processSentence(value);
    }

    @Override
    public UUID getCharacteristic() {
        return rangefinderCharacteristic;
    }

    private void processSentence(@NonNull byte[] value) {
        if (value[0] == statusLocation) {
            Log.i(TAG, "ap -> phone: location");
        } else {
            Log.w(TAG, "ap -> phone: unknown " + Util.byteArrayToHex(value));
        }
    }

    private void requestRangefinderService() {
        final BluetoothGattService service = bluetoothGatt.getService(rangefinderService);
        final BluetoothGattCharacteristic ch = service.getCharacteristic(rangefinderCharacteristic);
        if (ch != null) {
            // Enables notification locally:
            bluetoothGatt.setCharacteristicNotification(ch, true);
            // Enables notification on the device
            final BluetoothGattDescriptor descriptor = ch.getDescriptor(clientCharacteristicDescriptor);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    private void sendLandingZone(LandingZone lz) {
        Log.d(TAG, "app -> device: lz " + lz);
        final BluetoothGattService service = bluetoothGatt.getService(rangefinderService);
        final BluetoothGattCharacteristic ch = service.getCharacteristic(rangefinderCharacteristic);
        if (ch != null) {
            final byte[] value = {setLandingZone, 0x00};
            ch.setValue(value);
            bluetoothGatt.writeCharacteristic(ch);
        }
    }

}
