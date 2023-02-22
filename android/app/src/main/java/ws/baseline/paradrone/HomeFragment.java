package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.BluetoothState;
import ws.baseline.paradrone.databinding.HomeFragmentBinding;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import timber.log.Timber;

public class HomeFragment extends Fragment {
    private HomeFragmentBinding binding;
    @NonNull
    private Permissions permissions = Permissions.getPermissions(null);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);
        checkConnected();

        final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result) {
                        Timber.i("Location permission granted");
                        // Re-try bluetooth
                        final Activity activity = getActivity();
                        if (activity != null) {
                            Services.bluetooth.start(activity);
                        }
                    } else {
                        Timber.w("Location permission denied");
                    }
                }
        );

        final View.OnClickListener clickListener = view -> {
            if (!permissions.locationPermission) {
                // Request location permissions
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            } else if (!permissions.locationEnabled) {
                // Open location settings so user can enable location
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } else if (!permissions.bluetoothPermission) {
                // TODO: Request bluetooth permission
            } else if (!permissions.bluetoothEnabled) {
                // Enable bluetooth if needed
                final Activity activity = this.getActivity();
                if (activity != null) {
                    Services.bluetooth.enable(activity);
                }
            }
        };
        binding.homeText.setOnClickListener(clickListener);
        binding.permissionButton.setOnClickListener(clickListener);
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
     * Check permissions, and if connected, change to CTRL fragment
     */
    private void checkConnected() {
        permissions = Permissions.getPermissions(getContext());
        if (!permissions.locationPermission) {
            binding.homeText.setText(R.string.location_permission);
            binding.permissionButton.setText(R.string.grant_permission);
            binding.permissionButton.setVisibility(View.VISIBLE);
        } else if (!permissions.locationEnabled) {
            binding.homeText.setText(R.string.location_disabled);
            binding.permissionButton.setText(R.string.location_disabled_action);
            binding.permissionButton.setVisibility(View.VISIBLE);
        } else if (!permissions.bluetoothPermission) {
            binding.homeText.setText(R.string.bluetooth_permission);
            binding.permissionButton.setText(R.string.grant_permission);
            binding.permissionButton.setVisibility(View.VISIBLE);
        } else if (!permissions.bluetoothEnabled) {
            binding.homeText.setText(R.string.bluetooth_disabled);
            binding.permissionButton.setText(R.string.bluetooth_disabled_action);
            binding.permissionButton.setVisibility(View.VISIBLE);
        } else {
            binding.homeText.setText(R.string.bluetooth_searching);
            binding.permissionButton.setVisibility(View.GONE);
        }
        if (Services.bluetooth.isConnected()) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.control_plane, new ControlFragment())
                    .commit();
        }
    }

}
