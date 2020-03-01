package ws.baseline.autopilot.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BluetoothPreferences {

    private static final String PREF_BT_DEVICE_ID = "bluetooth_id";

    // Android shared preferences for bluetooth
    @Nullable
    public static String preferenceDeviceId = null;

    public static void load(@NonNull SharedPreferences prefs) {
        preferenceDeviceId = prefs.getString(PREF_BT_DEVICE_ID, preferenceDeviceId);
    }

    public static void save(@NonNull Context context, String deviceId) {
        preferenceDeviceId = deviceId;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString(PREF_BT_DEVICE_ID, preferenceDeviceId);
        edit.apply();
    }

}
