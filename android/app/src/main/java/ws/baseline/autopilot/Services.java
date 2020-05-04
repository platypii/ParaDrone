package ws.baseline.autopilot;

import ws.baseline.autopilot.bluetooth.BluetoothService;
import ws.baseline.autopilot.plan.FlightComputer;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;

public class Services {
    private static final String TAG = "Services";

    public static final BluetoothService bluetooth = new BluetoothService();
    public static final FlightComputer flightComputer = new FlightComputer();

    public static void start(@NonNull Activity activity) {
        Log.i(TAG, "Starting services...");
        bluetooth.start(activity);
        flightComputer.start();
    }

    static void stop() {
        Log.i(TAG, "Stopping services...");
        flightComputer.stop();
        bluetooth.stop();
    }
}
