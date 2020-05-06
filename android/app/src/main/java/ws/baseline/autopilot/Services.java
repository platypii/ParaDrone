package ws.baseline.autopilot;

import ws.baseline.autopilot.bluetooth.BluetoothService;
import ws.baseline.autopilot.plan.FlightComputer;

import android.app.Activity;
import androidx.annotation.NonNull;
import timber.log.Timber;

public class Services {

    public static final BluetoothService bluetooth = new BluetoothService();
    public static final FlightComputer flightComputer = new FlightComputer();

    public static void start(@NonNull Activity activity) {
        Timber.i("Starting services...");
        bluetooth.start(activity);
        flightComputer.start();
    }

    static void stop() {
        Timber.i("Stopping services...");
        flightComputer.stop();
        bluetooth.stop();
    }
}
