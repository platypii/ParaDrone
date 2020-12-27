package ws.baseline.paradrone.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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

    private static final int ENABLE_BLUETOOTH_CODE = 13;

    @NonNull
    private final BluetoothPreferences prefs = new BluetoothPreferences();
    public BluetoothPreferences.DeviceMode deviceMode = AP;

    // Bluetooth state
    private int bluetoothState = BT_STOPPED;
    @Nullable
    BluetoothHandler bluetoothHandler;

    @NonNull
    public final AutopilotActions actions = new AutopilotActions(this);

    public void start(@NonNull Context context) {
        deviceMode = prefs.load(context);
        if (bluetoothState == BT_STOPPED) {
            bluetoothState = BT_STARTED;
            // TODO: Prompt to start bluetooth if needed
            // Start bluetooth thread
            if (bluetoothHandler == null) {
                bluetoothHandler = new BluetoothHandler(this, context);
            }
            bluetoothHandler.start();
        } else {
            Timber.e("Bluetooth already started: %s", BT_STATES[bluetoothState]);
        }
    }

    public void setDeviceMode(BluetoothPreferences.DeviceMode mode) {
        if (deviceMode != mode) {
            deviceMode = mode;
            EventBus.getDefault().post(mode);
            if (bluetoothHandler != null) {
                bluetoothHandler.disconnect();
            }
        }
    }

    /**
     * Get bluetooth adapter, request bluetooth if needed
     */
    @Nullable
    private BluetoothAdapter getAdapter(@NonNull Activity activity) {
        // TODO: Make sure this doesn't take too long
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device not supported
            Timber.e("Bluetooth not supported");
        } else if (!bluetoothAdapter.isEnabled()) {
            // Turn on bluetooth
            // TODO: Handle result?
            final Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBluetoothIntent, ENABLE_BLUETOOTH_CODE);
        }
        return bluetoothAdapter;
    }

    public int getState() {
        return bluetoothState;
    }

    public boolean isConnected() {
        return bluetoothState == BT_CONNECTED;
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
