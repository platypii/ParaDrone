package ws.baseline.paradrone;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Permissions {
    public final boolean bluetoothPermission;
    public final boolean bluetoothEnabled;
    public final boolean locationPermission;
    public final boolean locationEnabled;

    public static final int RC_BLUE = 5;

    private static final String[] locationPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static Permissions getPermissions(@Nullable Context context) {
        return context != null ? new Permissions(context) : new Permissions();
    }

    private Permissions() {
        bluetoothPermission = false;
        bluetoothEnabled = false;
        locationPermission = false;
        locationEnabled = false;
    }
    private Permissions(@NonNull Context context) {
        bluetoothPermission = hasBluetoothPermissions(context);
        bluetoothEnabled = isBluetoothEnabled();
        locationPermission = hasLocationPermissions(context);
        locationEnabled = checkLocationEnabled(context);
    }

    public static boolean hasLocationPermissions(@NonNull Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED;
    }

    private boolean checkLocationEnabled(@NonNull Context context) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locationManager.isLocationEnabled();
        } else {
            final boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            final boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            return isGpsEnabled || isNetworkEnabled;
        }
    }


    /**
     * Check for bluetooth permissions depending on android version
     */
    public static boolean hasBluetoothPermissions(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return hasBluetoothConnectPermissions(context) && hasBluetoothScanPermissions(context);
        } else {
            return hasLocationPermissions(context);
        }
    }

    public static void requestBluetoothPermissions(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(activity, btPermissions(), RC_BLUE);
        }
    }

    public static boolean hasBluetoothConnectPermissions(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private static boolean hasBluetoothScanPermissions(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PERMISSION_GRANTED;
        } else {
            return true; // TODO: check for location permission on older android?
        }
    }

    private boolean isBluetoothEnabled() {
        return Services.bluetooth.isEnabled();
    }

    private static String[] btPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            return locationPermissions;
        }
    }
}
