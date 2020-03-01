package ws.baseline.autopilot;

import ws.baseline.autopilot.bluetooth.BluetoothState;
import ws.baseline.autopilot.databinding.FragmentStatusBinding;
import ws.baseline.autopilot.util.Convert;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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

    @Subscribe
    public void onBluetoothState(@NonNull BluetoothState event) {
        update();
    }

    @Subscribe
    public void onDroneState(@NonNull DroneState event) {
        update();
    }

    /**
     * Update views
     */
    private void update() {
        binding.statusConnect.setText("Connect status...");
        final DroneState drone = DroneState.get();
        if (drone != null) {
            binding.statusLocation.setText(drone.currentLocation.toString());
            binding.statusAltitude.setText(Convert.distance3(drone.currentLocation.alt - drone.lz.destination.alt) + " AGL");
            binding.statusDistance.setText("Distance...");
        } else {
            binding.statusLocation.setText("");
            binding.statusAltitude.setText("");
            binding.statusDistance.setText("");
        }
    }

}
