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

    private static boolean started = false;

    public static void start(@NonNull Activity activity) {
        if (!started) {
            started = true;
            Timber.i("Starting services...");
            bluetooth.start(activity);
//            flightComputer.start();
            location.start();
        } else {
            Timber.e("Services started twice");
        }
    }

    public static void stop() {
        if (!started) {
            Timber.e("Stop called but services not started");
        }
        Timber.i("Stopping services...");
//        flightComputer.stop();
        bluetooth.stop();
        location.stop();
        started = false;
    }
}
