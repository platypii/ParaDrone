package ws.baseline.autopilot;

import ws.baseline.autopilot.bluetooth.APEvent;
import ws.baseline.autopilot.bluetooth.APLandingZone;
import ws.baseline.autopilot.bluetooth.APLocationMsg;
import ws.baseline.autopilot.bluetooth.BluetoothState;
import ws.baseline.autopilot.databinding.FragmentStatusBinding;
import ws.baseline.autopilot.geo.Geo;
import ws.baseline.autopilot.geo.LandingZone;
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
        if (Services.bluetooth.getState() == BluetoothState.BT_CONNECTED) {
            binding.statusBt.setTextColor(0xff11ccff);
        } else {
            binding.statusBt.setTextColor(0xff666666);
        }
        binding.statusConnect.setText("BT: " + BluetoothState.toString(Services.bluetooth.getState()));
        final APLocationMsg ll = APLocationMsg.lastLocation;
        final LandingZone lz = APLandingZone.lastLz != null ? APLandingZone.lastLz.lz : null;

        // LL
        if (ll != null) {
            binding.statusLocation.setText("LL: " + ll);
        } else {
            binding.statusLocation.setText("LL:");
        }

        // LZ
        if (ll != null && lz != null) {
            final double distance = Geo.distance(ll.lat, ll.lng, lz.destination.lat, lz.destination.lng);
            final double bearing = Geo.bearing(ll.lat, ll.lng, lz.destination.lat, lz.destination.lng);
            binding.statusLandingZone.setText("LZ: " + Convert.distance(distance) + " AGL  " + Convert.bearing2(bearing));
        } else {
            binding.statusLandingZone.setText("LZ:");
        }

        // Alt
        if (ll != null && !Double.isNaN(ll.alt)) {
            if (lz != null) {
                binding.statusAltitude.setText("Alt: " + Convert.distance(ll.alt - lz.destination.alt, 1, true) + " AGL");
            } else {
                binding.statusAltitude.setText("Alt: " + Convert.distance(ll.alt, 1, true) + " MSL");
            }
        } else {
            binding.statusAltitude.setText("Alt:");
        }
    }

}
