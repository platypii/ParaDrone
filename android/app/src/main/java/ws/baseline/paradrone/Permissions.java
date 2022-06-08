package ws.baseline.paradrone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class Permissions {
    public final boolean bluetoothPermission;
    public final boolean bluetoothEnabled;
    public final boolean locationPermission;
    public final boolean locationEnabled;

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
        bluetoothPermission = checkBluetoothPermission(context);
        bluetoothEnabled = checkBluetoothEnabled(context);
        locationPermission = checkLocationPermission(context);
        locationEnabled = checkLocationEnabled(context);
    }

    private boolean checkLocationPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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

    private boolean checkBluetoothPermission(@NonNull Context context) {
        // TODO: Check additional bluetooth permissions
        return true; // TODO: maxsdk30
    }

    private boolean checkBluetoothEnabled(@NonNull Context context) {
        return Services.bluetooth.isEnabled();
    }

}
