package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.ApLandingZone;
import ws.baseline.paradrone.bluetooth.BluetoothPreferences;
import ws.baseline.paradrone.bluetooth.BluetoothState;
import ws.baseline.paradrone.databinding.ApScreenBinding;
import ws.baseline.paradrone.geo.Geo;
import ws.baseline.paradrone.geo.GeoPoint;
import ws.baseline.paradrone.geo.LandingZone;
import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.util.Convert;

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
    @NonNull
    private final Handler handler = new Handler();
    Runnable updateRunner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ApScreenBinding.inflate(inflater, container, false);
        binding.statusLandingZone.setOnClickListener((event) -> {
            // Refresh LZ
            Services.bluetooth.actions.fetchLandingZone();
            if (ApLandingZone.lastLz != null && ApLandingZone.lastLz.lz != null) {
                ApLandingZone.setPending(ApLandingZone.lastLz.lz);
            }
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
    public void onApLandingZone(@NonNull ApLandingZone event) {
        update();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApEvent(@NonNull GeoPoint event) {
        update();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceMode(@NonNull BluetoothPreferences.DeviceMode event) {
        update();
    }

    /**
     * Update views
     */
    @SuppressLint("SetTextI18n")
    private void update() {
        final GeoPoint ll = Services.location.lastLoc;
        final LandingZone lz = ApLandingZone.lastLz != null ? ApLandingZone.lastLz.lz : null;

        // LL
        if (ll != null) {
            binding.statusLocation.setText(String.format(Locale.getDefault(), "%.6f, %6f", ll.lat, ll.lng));
        } else {
            binding.statusLocation.setText("");
        }

        // Alt
        if (ll != null && !Double.isNaN(ll.alt)) {
            if (lz != null) {
                binding.statusAltitude.setText(Convert.distance(ll.alt - lz.destination.alt, 0, true) + "mAGL");
            } else {
                binding.statusAltitude.setText(Convert.distance(ll.alt, 0, true) + "mMSL");
            }
        }

        // Ground speed
        if (ll != null && !Double.isNaN(ll.groundSpeed())) {
            final double vel = ll.groundSpeed();
            binding.statusSpeed.setText(String.format(Locale.getDefault(), "%.0f mph", vel / Convert.MPH));
        } else {
            binding.statusSpeed.setText("");
        }

        // GPS last fix
        final long delta = System.currentTimeMillis() - Services.location.lastMillis;
        if (delta < 60000) {
            binding.statusLastFix.setText(String.format(Locale.getDefault(), "%ds", delta / 1000));
        } else if (delta < 3600000) {
            binding.statusLastFix.setText(String.format(Locale.getDefault(), "%dm", delta / 60000));
        } else if (delta < 86400000) {
            binding.statusLastFix.setText(String.format(Locale.getDefault(), "%dh", delta / 3600000));
        }

        // LZ
        if (ll != null && lz != null) {
            final double distance = Geo.distance(ll.lat, ll.lng, lz.destination.lat, lz.destination.lng);
            final double bearing = Geo.bearing(ll.lat, ll.lng, lz.destination.lat, lz.destination.lng);
            binding.statusLandingZone.setText("LZ " + Convert.distance3(distance) + " " + Convert.bearing3(bearing));
        } else if (lz != null) {
            binding.statusLandingZone.setText(String.format(Locale.getDefault(), "LZ %.1f, %.1f, %.0fm", lz.destination.lat, lz.destination.lng, lz.destination.alt));
        }

        // Flight mode
        final Path plan = Services.flightComputer.plan;
        if (plan != null) {
            binding.statusFlightMode.setText(plan.name);
        } else {
            binding.statusFlightMode.setText("");
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
