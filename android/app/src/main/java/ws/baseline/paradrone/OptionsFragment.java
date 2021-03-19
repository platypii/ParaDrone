package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.BluetoothPreferences;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class OptionsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.options, rootKey);
        findPreference("device_mode_rc").setOnPreferenceChangeListener(this);
        findPreference("set_lz").setOnPreferenceClickListener(this);
        findPreference("set_config").setOnPreferenceClickListener(this);
        findPreference("start_web").setOnPreferenceClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewState.setMode(ViewState.ViewMode.OPTIONS);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "set_lz":
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.control_plane, new LandingFragment())
                        .addToBackStack(null)
                        .commit();
                break;
            case "set_config":
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.control_plane, new ConfigFragment())
                        .addToBackStack(null)
                        .commit();
                break;
            case "start_web":
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.control_plane, new WebServerFragment())
                        .addToBackStack(null)
                        .commit();
                break;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("device_mode_rc".equals(preference.getKey())) {
            if ((Boolean) newValue) {
                Services.bluetooth.setDeviceMode(BluetoothPreferences.DeviceMode.RC);
            } else {
                Services.bluetooth.setDeviceMode(BluetoothPreferences.DeviceMode.AP);
            }
        }
        return true;
    }
}
