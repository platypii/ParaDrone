package ws.baseline.autopilot;

import android.annotation.SuppressLint;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Show the user their GPS status
 */
public class SignalStatus extends Fragment {
    private TextView signalStatus;

    // Periodic UI updates
    private static final int signalUpdateInterval = 200; // milliseconds
    private final Handler handler = new Handler();
    private final Runnable signalRunnable = new Runnable() {
        public void run() {
            update();
            handler.postDelayed(this, signalUpdateInterval);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.status_panel, container, false);
        signalStatus = view.findViewById(R.id.signalStatus);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Initial update
        update();

        // Start signal updates
        handler.post(signalRunnable);

        // Listen for location updates
        EventBus.getDefault().register(this);
    }

    /**
     * Update the views for GPS signal strength
     */
    @SuppressLint("SetTextI18n")
    private void update() {
        final long lastFixDuration = Services.location.lastFixDuration();
        if (lastFixDuration < 0) {
            // No fix yet
            signalStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_red, 0, 0, 0);
            signalStatus.setText("GPS searching...");
        } else {
            // How many of the last X expected fixes have we missed?
            if (lastFixDuration > 10000) {
                signalStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_red, 0, 0, 0);
                signalStatus.setText("GPS last fix " + lastFixDuration / 1000L + "s");
            } else if (lastFixDuration > 2000) {
                signalStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_yellow, 0, 0, 0);
                signalStatus.setText("GPS last fix " + lastFixDuration / 1000L + "s");
            } else {
                signalStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_green, 0, 0, 0);
                signalStatus.setText(String.format(Locale.getDefault(), "GPS %.2fHz", Services.location.refreshRate.refreshRate));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationChanged(@NonNull GeoPoint loc) {
        update();
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(signalRunnable);
        EventBus.getDefault().unregister(this);
    }

}
