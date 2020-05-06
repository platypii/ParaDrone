package ws.baseline.autopilot.bluetooth;

import ws.baseline.autopilot.geo.LandingZone;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;
import timber.log.Timber;

import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_CONNECTING;
import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_STARTING;
import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_STATES;
import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_STOPPED;
import static ws.baseline.autopilot.bluetooth.BluetoothState.BT_STOPPING;

/**
 * Class to manage a bluetooth GPS receiver.
 * Note: instantiating this class will not automatically start bluetooth. Call startAsync to connect.
 */
public class BluetoothService {

    private static final int ENABLE_BLUETOOTH_CODE = 13;

    // Bluetooth state
    private int bluetoothState = BT_STOPPED;
    @Nullable
    private BluetoothRunnable bluetoothRunnable;
    @Nullable
    private Thread bluetoothThread;

    public void start(@NonNull Activity activity) {
        if (bluetoothState == BT_STOPPED) {
            setState(BT_STARTING);
            // Start bluetooth thread
            if (bluetoothRunnable != null) {
                Timber.e("Bluetooth thread already started");
            }
            final BluetoothAdapter bluetoothAdapter = getAdapter(activity);
            if (bluetoothAdapter != null) {
                bluetoothRunnable = new BluetoothRunnable(BluetoothService.this, activity, bluetoothAdapter);
                bluetoothThread = new Thread(bluetoothRunnable);
                bluetoothThread.start();
            }
        } else {
            Timber.e("Bluetooth already started: %s", BT_STATES[bluetoothState]);
        }
    }
    public void setLandingZone(LandingZone lz) {
        if (bluetoothRunnable != null && bluetoothRunnable.protocol != null) {
            bluetoothRunnable.protocol.setLandingZone(lz);
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

    public synchronized void stop() {
        if (bluetoothState != BT_STOPPED) {
            Timber.i("Stopping bluetooth service");
            // Stop thread
            if (bluetoothRunnable != null && bluetoothThread != null) {
                bluetoothRunnable.stop();
                try {
                    bluetoothThread.join(1000);

                    // Thread is dead, clean up
                    bluetoothRunnable = null;
                    bluetoothThread = null;
                    if (bluetoothState != BT_STOPPED) {
                        Timber.e("Unexpected bluetooth state, should be STOPPED when thread has stopped: %s", BT_STATES[bluetoothState]);
                    }
                } catch (InterruptedException e) {
                    Timber.e(e, "Bluetooth thread interrupted while waiting for it to die");
                }
                Timber.i("Bluetooth service stopped");
            } else {
                Timber.e("Cannot stop bluetooth, runnable is null: %s", BT_STATES[bluetoothState]);
                // Set state to stopped since it prevents getting stuck in state STOPPING
            }
            setState(BT_STOPPED);
        }
    }
}
