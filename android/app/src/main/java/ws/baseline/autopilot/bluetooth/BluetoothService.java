package ws.baseline.autopilot.bluetooth;

import ws.baseline.autopilot.geo.LandingZone;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    private static final String TAG = "Bluetooth";

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
                Log.e(TAG, "Bluetooth thread already started");
            }
            final BluetoothAdapter bluetoothAdapter = getAdapter(activity);
            if (bluetoothAdapter != null) {
                bluetoothRunnable = new BluetoothRunnable(BluetoothService.this, activity, bluetoothAdapter);
                bluetoothThread = new Thread(bluetoothRunnable);
                bluetoothThread.start();
            }
        } else {
            Log.e(TAG, "Bluetooth already started: " + BT_STATES[bluetoothState]);
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
            Log.e(TAG, "Bluetooth not supported");
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
            Log.e(TAG, "Invalid bluetooth state transition: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        }
        if (bluetoothState == state) {
            Log.e(TAG, "Null state transition: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        }
        Log.d(TAG, "Bluetooth state: " + BT_STATES[bluetoothState] + " -> " + BT_STATES[state]);
        bluetoothState = state;
        // TODO: Emit
        // EventBus.getDefault().post(new BluetoothState(state));
    }

    public synchronized void stop() {
        if (bluetoothState != BT_STOPPED) {
            Log.i(TAG, "Stopping bluetooth service");
            // Stop thread
            if (bluetoothRunnable != null && bluetoothThread != null) {
                bluetoothRunnable.stop();
                try {
                    bluetoothThread.join(1000);

                    // Thread is dead, clean up
                    bluetoothRunnable = null;
                    bluetoothThread = null;
                    if (bluetoothState != BT_STOPPED) {
                        Log.e(TAG, "Unexpected bluetooth state, should be STOPPED when thread has stopped: " + BT_STATES[bluetoothState]);
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Bluetooth thread interrupted while waiting for it to die", e);
                }
                Log.i(TAG, "Bluetooth service stopped");
            } else {
                Log.e(TAG, "Cannot stop bluetooth, runnable is null: " + BT_STATES[bluetoothState]);
                // Set state to stopped since it prevents getting stuck in state STOPPING
            }
            setState(BT_STOPPED);
        }
    }
}
