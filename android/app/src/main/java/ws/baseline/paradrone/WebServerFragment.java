package ws.baseline.paradrone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import ws.baseline.paradrone.bluetooth.BluetoothState;
import ws.baseline.paradrone.bluetooth.UrlMsg;
import ws.baseline.paradrone.databinding.WebFragmentBinding;

/**
 * Autopilot configuration. Frequency, stroke length, motor direction.
 */
public class WebServerFragment extends Fragment {
    private WebFragmentBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = WebFragmentBinding.inflate(inflater, container, false);
        binding.webServerUrl.setOnClickListener((e) -> {
            // Open web browser
            final String url = binding.webServerUrl.getText().toString();
            if (!url.isEmpty()) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://" + url));
                startActivity(intent);
            }
        });
        binding.startWeb.setOnClickListener((e) -> send());
        loadForm();

        // Use cached url if available
        if (UrlMsg.lastUrl != null) {
            binding.webServerUrl.setText(UrlMsg.lastUrl.url);
        }

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewState.setMode(ViewState.ViewMode.CFG);
        EventBus.getDefault().register(this);
        onBluetoothState(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onBluetoothState(@Nullable BluetoothState bt) {
        binding.startWeb.setEnabled(Services.bluetooth.isConnected());
    }

    @Subscribe
    public void onWebServerMsg(@NonNull UrlMsg msg) {
        // Update views
        binding.webServerUrl.setText(msg.url);
    }

    private void send() {
        // Load from form
        final String ssid = binding.wifiSsid.getText().toString();
        final String password = binding.wifiPassword.getText().toString();
        if (!ssid.isEmpty()) {
            binding.webServerUrl.setText("");
            Services.bluetooth.actions.startWebServer(ssid, password);
            saveForm(ssid, password);
        } else {
            Toast.makeText(getContext(), "Wifi SSID name required", Toast.LENGTH_SHORT).show();
            binding.wifiSsid.requestFocus();
        }
    }

    private void saveForm(String ssid, String password) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString("wifi_ssid", ssid);
        edit.putString("wifi_password", password);
        edit.apply();
    }

    private void loadForm() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        binding.wifiSsid.setText(prefs.getString("wifi_ssid", ""));
        binding.wifiPassword.setText(prefs.getString("wifi_password", ""));
    }
}
