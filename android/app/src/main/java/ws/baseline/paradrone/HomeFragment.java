package ws.baseline.paradrone;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import ws.baseline.paradrone.bluetooth.BluetoothState;
import ws.baseline.paradrone.databinding.HomeFragmentBinding;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class HomeFragment extends Fragment {
    private HomeFragmentBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);
        binding.homeText.setOnClickListener(view -> {
            if (!locationEnabled()) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } else if (!Services.bluetooth.isEnabled()) {
                // Enable bluetooth if needed
                final Activity activity = this.getActivity();
                if (activity != null) {
                    Services.bluetooth.enable(activity);
                }
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewState.setMode(ViewState.ViewMode.HOME);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkConnected();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onBluetoothState(BluetoothState bt) {
        checkConnected();
    }

    /**
     * If connected, change to CTRL fragment
     */
    private void checkConnected() {
        if (!locationEnabled()) {
            binding.homeText.setText(R.string.location_disabled);
        } else if (Services.bluetooth.isEnabled()) {
            binding.homeText.setText(R.string.bluetooth_searching);
        } else {
            binding.homeText.setText(R.string.bluetooth_disabled);
        }
        if (Services.bluetooth.isConnected()) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.control_plane, new ControlFragment())
                    .commit();
        }
    }

    private boolean locationEnabled() {
        final Context context = getContext();
        if (context != null) {
            final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return locationManager.isLocationEnabled();
            } else {
                final boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                final boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                return isGpsEnabled || isNetworkEnabled;
            }
        } else {
            return false;
        }
    }
}
