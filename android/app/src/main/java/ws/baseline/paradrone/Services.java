package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.BluetoothService;
import ws.baseline.paradrone.plan.FlightComputer;

import android.app.Activity;
import androidx.annotation.NonNull;
import timber.log.Timber;

public class Services {

    public static final BluetoothService bluetooth = new BluetoothService();
    public static final FlightComputer flightComputer = new FlightComputer();
    public static final LocationService location = new LocationService();

    public static void start(@NonNull Activity activity) {
        Timber.i("Starting services...");
        bluetooth.start(activity);
        flightComputer.start();
        location.start();
    }

    static void stop() {
        Timber.i("Stopping services...");
        flightComputer.stop();
        bluetooth.stop();
        location.stop();
    }
}
