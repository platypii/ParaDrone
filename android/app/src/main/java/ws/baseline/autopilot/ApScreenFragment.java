package ws.baseline.autopilot;

import ws.baseline.autopilot.bluetooth.APLandingZone;
import ws.baseline.autopilot.bluetooth.BluetoothState;
import ws.baseline.autopilot.databinding.ApScreenBinding;
import ws.baseline.autopilot.geo.Geo;
import ws.baseline.autopilot.geo.LandingZone;
import ws.baseline.autopilot.util.Convert;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Display what the drone should be displaying on its screen
 */
public class ApScreenFragment extends Fragment {
    private ApScreenBinding binding;
    private Handler handler = new Handler();
    Runnable updateRunner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ApScreenBinding.inflate(inflater, container, false);
        binding.statusLandingZone.setOnClickListener((event) -> {
            // Refresh LZ
            Services.bluetooth.fetchLandingZone();
            if (APLandingZone.lastLz != null && APLandingZone.lastLz.lz != null) {
                APLandingZone.setPending(APLandingZone.lastLz.lz);
            }
        });
        binding.statusBt.setOnClickListener((event) -> {
            Services.bluetooth.switchDeviceMode();
            update();
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        updateRunner = () -> {
            update();
            handler.postDelayed(updateRunner, 1000);
        };
        updateRunner.run();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        handler.removeCallbacks(updateRunner);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothState(@NonNull BluetoothState event) {
        update();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApLandingZone(@NonNull APLandingZone event) {
        update();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApEvent(@NonNull GeoPoint event) {
        update();
    }

    /**
     * Update views
     */
    @SuppressLint("SetTextI18n")
    private void update() {
        final GeoPoint ll = Services.location.lastPoint;
        final LandingZone lz = APLandingZone.lastLz != null ? APLandingZone.lastLz.lz : null;

        // LL
        if (ll != null) {
            binding.statusLocation.setText(String.format(Locale.getDefault(), "%.6f, %6f", ll.lat, ll.lng));
            if (Services.location.lastMillis >= System.currentTimeMillis() - 5000) {
                binding.statusLocation.setTextColor(0xff11ccff);
            } else {
                binding.statusLocation.setTextColor(0xff666666);
            }
        } else {
            binding.statusLocation.setText("");
        }

        // LZ
        if (ll != null && lz != null) {
            final double distance = Geo.distance(ll.lat, ll.lng, lz.destination.lat, lz.destination.lng);
            final double bearing = Geo.bearing(ll.lat, ll.lng, lz.destination.lat, lz.destination.lng);
            binding.statusLandingZone.setText("LZ: " + Convert.distance(distance) + " " + Convert.bearing2(bearing));
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

        // BT
        binding.statusBt.setText(Services.bluetooth.getBtString());
        if (Services.bluetooth.getState() == BluetoothState.BT_CONNECTED) {
            binding.statusBt.setTextColor(0xff11ccff);
        } else {
            binding.statusBt.setTextColor(0xff666666);
        }
    }

}
