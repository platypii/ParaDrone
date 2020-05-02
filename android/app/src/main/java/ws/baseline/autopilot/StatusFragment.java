package ws.baseline.autopilot;

import ws.baseline.autopilot.bluetooth.APEvent;
import ws.baseline.autopilot.bluetooth.APLocationEvent;
import ws.baseline.autopilot.bluetooth.BluetoothState;
import ws.baseline.autopilot.databinding.FragmentStatusBinding;
import ws.baseline.autopilot.util.Convert;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Display drone status
 */
public class StatusFragment extends Fragment {
    private FragmentStatusBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        update();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothState(@NonNull BluetoothState event) {
        update();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApEvent(@NonNull APEvent event) {
        update();
    }

    /**
     * Update views
     */
    @SuppressLint("SetTextI18n")
    private void update() {
        // Bluetooth state
        binding.statusConnect.setText("BT: " + BluetoothState.toString(Services.bluetooth.getState()));
        if (APLocationEvent.lastLocation != null) {
            binding.statusLocation.setText("LL: " + APLocationEvent.lastLocation);
        } else {
            binding.statusLocation.setText("LL:");
        }
        final DroneState drone = DroneState.get();
        if (drone != null) {
            binding.statusLandingZone.setText("LZ: " + drone.lz);
            binding.statusAltitude.setText(Convert.distance3(drone.currentLocation.alt - drone.lz.destination.alt) + " AGL");
            binding.statusDistance.setText("Dist:");
        } else {
            binding.statusLandingZone.setText("LZ: not set");
            binding.statusAltitude.setText("Alt:");
            binding.statusDistance.setText("Dist:");
        }
    }

}
