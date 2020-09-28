package ws.baseline.paradrone.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import static ws.baseline.paradrone.bluetooth.BluetoothPreferences.DeviceMode.AP;
import static ws.baseline.paradrone.bluetooth.BluetoothPreferences.DeviceMode.RC;

public class BluetoothPreferences {
    private static final String PREF_RC_MODE = "device_mode_rc";

    public enum DeviceMode {AP, RC}

    private SharedPreferences prefs;

    public DeviceMode load(@NonNull Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_RC_MODE, false) ? RC : AP;
    }

    public void save(DeviceMode deviceMode) {
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(PREF_RC_MODE, deviceMode == RC);
        edit.apply();
    }
}
