package ws.baseline.paradrone.bluetooth;

import ws.baseline.paradrone.geo.LandingZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

public class AutopilotActions {
    @NonNull
    private final BluetoothService bt;

    public static final int MODE_IDLE = 0;
    public static final int MODE_AP = 1;

    public AutopilotActions(@NonNull BluetoothService bt) {
        this.bt = bt;
    }

    /**
     * Send new landing zone to the device.
     */
    public void setLandingZone(@Nullable LandingZone lz) {
        Timber.i("phone -> ap: set lz %s", lz);
        // Pack LZ into bytes
        final byte[] bytes = new ApLandingZone(lz, false).toBytes();
        sendCommand(bytes);
        // Fetch after send
        try {
            Thread.sleep(1000); // TODO: Wait for async callback
        } catch (InterruptedException ignored) {
        }
        fetchLandingZone();
    }

    /**
     * Send desired toggle position to the device.
     */
    public void setTogglePosition(int left, int right) {
        if (left < 0 || left > 255 || right < 0 || right > 255) {
            Timber.e("Invalid motor controls %d %d", left, right);
        }
        Timber.i("phone -> ap: set motor position %d %d", left & 0xff, right & 0xff);
        sendCommand(new byte[] {'T', (byte) left, (byte) right});
    }

    /**
     * Send desired motor speed to the device.
     */
    public void setMotorSpeed(int left, int right) {
        if (left < -127 || left > 127 || right < -127 || right > 127) {
            Timber.e("Invalid motor speed %d %d", left, right);
        }
        Timber.i("phone -> ap: set motor speed %d %d", left, right);
        sendCommand(new byte[] {'S', (byte) left, (byte) right});
    }

    /**
     * Send a request to the device for its target landing zone.
     */
    public void fetchLandingZone() {
        Timber.i("phone -> ap: fetch lz");
        sendCommand(new byte[] {'Q', 'Z'});
    }

    /**
     * Send a request to the device for its configuration.
     */
    public void fetchConfig() {
        Timber.i("phone -> ap: fetch config");
        sendCommand(new byte[] {'Q', 'C'});
    }

    /**
     * Send configuration settings to the device.
     */
    public void setConfig(@NonNull ApConfigMsg msg) {
        Timber.i("phone -> ap: set config %s", msg);
        sendCommand(msg.toBytes());
    }

    /**
     * Send requested flight mode () to the device.
     */
    public void setMode(int mode) {
        Timber.i("phone -> ap: set mode %d", mode);
        sendCommand(new byte[] {'M', (byte) mode});
    }

    /**
     * Send a request to the device to run a motor calibration routine.
     */
    public void calibrate() {
        Timber.i("phone -> ap: calibrate");
        sendCommand(new byte[] {'Q', 'I'});
    }

    /**
     * Send a request to the device for it to start a webserver for log access.
     */
    public void startWebServer(@NonNull String ssid, @NonNull String password) {
        Timber.i("phone -> ap: web init %s:%s", ssid, password);
        sendCommand(("W" + ssid + ":" + password).getBytes());
    }

    private void sendCommand(@NonNull byte[] value) {
        if (bt.bluetoothHandler != null) {
            bt.bluetoothHandler.sendCommand(value);
        }
    }
}
