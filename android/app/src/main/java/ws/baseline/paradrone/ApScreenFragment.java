package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.APLandingZone;
import ws.baseline.paradrone.bluetooth.BluetoothPreferences;
import ws.baseline.paradrone.bluetooth.BluetoothState;
import ws.baseline.paradrone.databinding.ApScreenBinding;
import ws.baseline.paradrone.geo.Geo;
import ws.baseline.paradrone.geo.LandingZone;
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
        binding.buttonAp.setOnClickListener((event) -> {
            Services.bluetooth.switchDeviceMode();
            update();
        });
        binding.buttonRl.setOnClickListener((event) -> {
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
        final GeoPoint ll = Services.location.lastLoc;
        final LandingZone lz = APLandingZone.lastLz != null ? APLandingZone.lastLz.lz : null;

        // LL
        if (ll != null) {
            binding.statusLocation.setText(String.format(Locale.getDefault(), "%.6f, %6f", ll.lat, ll.lng));
        } else {
            binding.statusLocation.setText("");
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
            binding.statusLandingZone.setText("LZ: " + Convert.distance(distance) + " " + Convert.bearing2(bearing));
        } else {
            binding.statusLandingZone.setText("LZ:");
        }

        // BT
        binding.statusBt.setText(Services.bluetooth.getBtString());
        if (Services.bluetooth.deviceMode == BluetoothPreferences.DeviceMode.AP) {
            binding.buttonAp.setEnabled(false);
            binding.buttonRl.setEnabled(true);
        } else {
            binding.buttonAp.setEnabled(true);
            binding.buttonRl.setEnabled(false);
        }
        if (Services.bluetooth.getState() == BluetoothState.BT_CONNECTED) {
            binding.statusBt.setTextColor(0xff11ccff);
        } else {
            binding.statusBt.setTextColor(0xff666666);
        }
    }

}
