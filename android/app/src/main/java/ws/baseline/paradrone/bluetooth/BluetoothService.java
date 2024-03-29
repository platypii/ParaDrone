package ws.baseline.paradrone.bluetooth;

import ws.baseline.paradrone.Permissions;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;
import timber.log.Timber;

import static ws.baseline.paradrone.bluetooth.BluetoothPreferences.DeviceMode.AP;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_CONNECTED;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_CONNECTING;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_STARTED;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_STATES;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_STOPPED;
import static ws.baseline.paradrone.bluetooth.BluetoothState.BT_STOPPING;

/**
 * Class to manage a bluetooth GPS receiver.
 * Note: instantiating this class will not automatically start bluetooth. Call startAsync to connect.
 */
public class BluetoothService {
    public static final int RC_BLUE_ENABLE = 13;

    @NonNull
    private final BluetoothPreferences prefs = new BluetoothPreferences();
    public BluetoothPreferences.DeviceMode deviceMode = AP;

    // Bluetooth state
    private int bluetoothState = BT_STOPPED;
    @Nullable
    BluetoothHandler bluetoothHandler;

    @NonNull
    public final AutopilotActions actions = new AutopilotActions(this);

    public void start(@NonNull Activity activity) {
        deviceMode = prefs.load(activity);
        final Permissions permissions = Permissions.getPermissions(activity);
        // TODO: Check for location on android pre-30
        if (!permissions.bluetoothEnabled) {
            Timber.e("Bluetooth disabled");
        } else if (!permissions.locationPermission) {
            Timber.e("Location permission required");
        } else if (!permissions.locationEnabled) {
            Timber.e("Location disabled");
        } else if (bluetoothState == BT_STOPPED) {
            bluetoothState = BT_STARTED;
            // Start bluetooth thread
            if (bluetoothHandler == null) {
                bluetoothHandler = new BluetoothHandler(this, activity);
            }
            bluetoothHandler.start();
        } else {
            Timber.e("Bluetooth already started: %s", BT_STATES[bluetoothState]);
        }
    }

    public void setDeviceMode(BluetoothPreferences.DeviceMode mode) {
        Timber.i("Bluetooth set device mode %s", mode);
        if (deviceMode != mode) {
            deviceMode = mode;
            EventBus.getDefault().post(mode);
            if (bluetoothHandler != null) {
                Timber.i("Restarting bluetooth");
                bluetoothHandler.disconnect();
            }
        }
    }

    public int getState() {
        return bluetoothState;
    }

    public boolean isConnected() {
        return bluetoothState == BT_CONNECTED;
    }

    public boolean isEnabled() {
        // TODO: Make sure this doesn't take too long
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Timber.w("This device has no bluetooth hardware");
            return false;
        } else {
            return bluetoothAdapter.isEnabled();
        }
    }

    /**
     * Request to enable bluetooth
     */
    public void enable(@NonNull Activity activity) {
        final Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBluetoothIntent, RC_BLUE_ENABLE);
    }

    void setState(int state) {
        if (bluetoothState == BT_STOPPING && state == BT_CONNECTING) {
            Timber.e("Invalid bluetooth state transition: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        }
        if (bluetoothState == state) {
            Timber.e("Null state transition: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        }
        Timber.d("Bluetooth state: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        bluetoothState = state;
        EventBus.getDefault().post(new BluetoothState(state));
    }

    /**
     * Return AP or RC device mode
     */
    @NonNull
    public String getBtString() {
        if (deviceMode == AP) {
            return "AP";
        } else {
            return "RC";
        }
    }

    public synchronized void stop() {
        if (bluetoothState != BT_STOPPED) {
            Timber.i("Stopping bluetooth service");
            // Stop thread
            if (bluetoothHandler != null) {
                bluetoothHandler.stop();
                if (bluetoothState == BT_STOPPED) {
                    Timber.i("Bluetooth service stopped");
                } else {
                    Timber.e("Unexpected bluetooth state, should be STOPPED when thread has stopped: %s", BT_STATES[bluetoothState]);
                }
            } else {
                Timber.e("Cannot stop bluetooth, handler is null: %s", BT_STATES[bluetoothState]);
            }
            // Always set state to stopped since it prevents getting stuck in state STOPPING
            setState(BT_STOPPED);
        }
    }
}
