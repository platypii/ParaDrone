package ws.baseline.paradrone.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import static ws.baseline.paradrone.bluetooth.BluetoothPreferences.DeviceMode.AP;
import static ws.baseline.paradrone.bluetooth.BluetoothPreferences.DeviceMode.RELAY;

public class BluetoothPreferences {
    private static final String PREF_RELAY_MODE = "bt_relay_mode";

    public enum DeviceMode {AP, RELAY}

    private SharedPreferences prefs;

    public DeviceMode load(@NonNull Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_RELAY_MODE, false) ? RELAY : AP;
    }

    public void save(DeviceMode deviceMode) {
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(PREF_RELAY_MODE, deviceMode == RELAY);
        edit.apply();
    }
}
