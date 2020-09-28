package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.BluetoothPreferences;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class ConfigFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.configs, rootKey);
        findPreference("device_mode_rc").setOnPreferenceChangeListener(this);
        findPreference("set_lz").setOnPreferenceClickListener(this);
        findPreference("set_freq").setOnPreferenceClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewState.setMode(ViewState.ViewMode.CFG);
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
            case "set_freq":
                final EditText edit = new EditText(getContext());
                edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                edit.setText("915000000");
                new AlertDialog.Builder(getContext())
                        .setTitle("Frequency")
                        .setView(edit)
                        .setPositiveButton(android.R.string.ok, (d, which) -> {
                            final int freq = Integer.parseInt(edit.getText().toString());
                            Services.bluetooth.actions.setFrequency(freq);
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                break;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case "device_mode_rc":
                if ((Boolean) newValue) {
                    Services.bluetooth.setDeviceMode(BluetoothPreferences.DeviceMode.RC);
                } else {
                    Services.bluetooth.setDeviceMode(BluetoothPreferences.DeviceMode.AP);
                }
                break;
        }
        return true;
    }
}
