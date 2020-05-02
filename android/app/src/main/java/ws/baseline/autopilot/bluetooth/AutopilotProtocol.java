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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private static final UUID characteristicLocation = UUID.fromString("00b45300-9235-47c8-b2f3-916cee33d85c");
    private static final UUID characteristicLz = UUID.fromString("00845300-ed55-43fa-bb54-8e721e0926ee");

    // Client Characteristic Configuration (what we subscribe to)
    private static final UUID clientCharacteristicDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

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
        return characteristicLocation;
    }

    private void processSentence(@NonNull byte[] value) {
        if (value[0] == 'L' && value.length == 19) {
            // 'L', millis, lat, lng, alt
            final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
            final long millis = buf.getLong(1);
            final double lat = buf.getInt(9) * 1e-6; // microdegrees
            final double lng = buf.getInt(13) * 1e-6; // microdegrees
            final double alt = buf.getShort(17) * 0.1; // decimeters
            Log.i(TAG, "ap -> phone: location " + lat + " " + lng + " " + alt);
            APLocationEvent.update(millis, lat, lng, alt);
        } else if (value[0] == 'S' && value.length == 15) {
            // 'S', millis, vN, vE, climb
            final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
            final long millis = buf.getLong(1);
            final double vD = buf.getShort(9) * 0.01; // cm/s
            final double vE = buf.getShort(11) * 0.01; // cm/s
            final double climb = buf.getShort(13) * 0.01; // cm/s
            Log.i(TAG, "ap -> phone: speed " + vD + " " + vE + " " + climb);
            APSpeedEvent.update(millis, vD, vE, climb);
        } else {
            Log.w(TAG, "ap -> phone: unknown " + Util.byteArrayToHex(value));
        }
    }

    private void requestAutopilotService() {
        final BluetoothGattService service = bluetoothGatt.getService(apService);
        final BluetoothGattCharacteristic ch = service.getCharacteristic(characteristicLocation);
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

    void setLandingZone(LandingZone lz) {
        Log.i(TAG, "phone -> ap: lz " + lz);
        final BluetoothGattService service = bluetoothGatt.getService(apService);
        final BluetoothGattCharacteristic ch = service.getCharacteristic(characteristicLz);
        if (ch != null) {
            // Pack LZ into bytes
            final byte[] value = new byte[13];
            value[0] = 'Z';
            final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
            buf.putInt(1, (int)(lz.destination.lat * 1e6)); // microdegrees
            buf.putInt(5, (int)(lz.destination.lng * 1e6)); // microdegrees
            buf.putShort(9, (short)(lz.destination.alt * 10)); // decimeters
            buf.putShort(11, (short)(lz.landingDirection * 1000)); // milliradians
            ch.setValue(value);
            bluetoothGatt.writeCharacteristic(ch);
        }
    }

}
