package ws.baseline.paradrone.bluetooth;

import ws.baseline.paradrone.geo.LandingZone;

import androidx.annotation.NonNull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import timber.log.Timber;

public class AutopilotActions {
    @NonNull
    private final BluetoothService bt;

    public static final int MODE_IDLE = 0;
    public static final int MODE_AP = 1;

    public AutopilotActions(@NonNull BluetoothService bt) {
        this.bt = bt;
    }

    public void setLandingZone(@NonNull LandingZone lz) {
        Timber.i("phone -> ap: set lz %s", lz);
        // Pack LZ into bytes
        final byte[] value = new byte[13];
        value[0] = 'Z';
        final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(1, (int)(lz.destination.lat * 1e6)); // microdegrees
        buf.putInt(5, (int)(lz.destination.lng * 1e6)); // microdegrees
        buf.putShort(9, (short)(lz.destination.alt * 10)); // decimeters
        buf.putShort(11, (short)(lz.landingDirection * 1000)); // milliradians
        sendCommand(value);
        // Fetch after send
        try {
            Thread.sleep(1000); // TODO: Async
        } catch (InterruptedException ignored) {
        }
        fetchLandingZone();
    }

    public void setMotorPosition(int left, int right) {
        if (left < 0 || left > 255 || right < 0 || right > 255) {
            Timber.e("Invalid motor controls %d %d", left, right);
        }
        Timber.i("phone -> ap: set motor position %d %d", left & 0xff, right & 0xff);
        sendCommand(new byte[] {'T', (byte) left, (byte) right});
    }

    public void setMotorSpeed(int left, int right) {
        if (left < -127 || left > 127 || right < -127 || right > 127) {
            Timber.e("Invalid motor speed %d %d", left, right);
        }
        Timber.i("phone -> ap: set motor speed %d %d", left, right);
        sendCommand(new byte[] {'S', (byte) left, (byte) right});
    }

    public void fetchLandingZone() {
        Timber.i("phone -> ap: fetch lz");
        sendCommand(new byte[] {'Q', 'Z'});
    }

    public void fetchMotorConfig() {
        Timber.i("phone -> ap: fetch motor config");
        sendCommand(new byte[] {'Q', 'C'});
    }

    public void setFrequency(int freq) {
        Timber.i("phone -> ap: set freq %d", freq);
        // Pack frequency into bytes
        final byte[] value = new byte[5];
        value[0] = 'F';
        final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(1, freq);
        sendCommand(value);
    }

    public void setMode(int mode) {
        Timber.i("phone -> ap: set mode %d", mode);
        sendCommand(new byte[] {'M', (byte) mode});
    }

    private void sendCommand(byte[] value) {
        if (bt.bluetoothHandler != null) {
            bt.bluetoothHandler.sendCommand(value);
        }
    }
}
