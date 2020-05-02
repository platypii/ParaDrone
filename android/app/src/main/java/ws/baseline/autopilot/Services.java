package ws.baseline.autopilot;

import ws.baseline.autopilot.bluetooth.BluetoothService;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;

public class Services {
    private static final String TAG = "Services";

    public static final BluetoothService bluetooth = new BluetoothService();

    public static void start(@NonNull Activity activity) {
        Log.i(TAG, "Starting services...");
        bluetooth.start(activity);
    }

    static void stop() {
        Log.i(TAG, "Stopping services...");
        bluetooth.stop();
    }
}
