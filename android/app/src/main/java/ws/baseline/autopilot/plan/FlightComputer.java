package ws.baseline.autopilot.plan;

import ws.baseline.autopilot.GeoPoint;
import ws.baseline.autopilot.bluetooth.APLandingZone;
import ws.baseline.autopilot.bluetooth.APLocation;
import ws.baseline.autopilot.bluetooth.APLocationMsg;
import ws.baseline.autopilot.bluetooth.APSpeedMsg;
import ws.baseline.autopilot.geo.LandingZone;
import ws.baseline.autopilot.geo.Path;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class FlightComputer {
    private static final String TAG = "FlightComputer";

    @Nullable
    public LandingZone lz;
    public boolean lzPending = false;

    // Combined location and speed data
    @Nullable
    public GeoPoint lastPoint;

    @Nullable
    public Path plan;

    // Raw bluetooth messages
    private APLocationMsg location;
    private APSpeedMsg speed;

    public void start() {
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onApLandingZone(@NonNull APLandingZone event) {
        this.lz = event.lz;
        this.lzPending = false;
    }

    @Subscribe
    public void onApLocation(@NonNull APLocationMsg event) {
        this.location = event;
    }

    @Subscribe
    public void onApSpeed(@NonNull APSpeedMsg event) {
        this.speed = event;
        if (location.millis != speed.millis) {
            Log.e(TAG, "Location/speed mismatch " + location.millis + " != " + speed.millis);
        }
        // Combine location and speed data
        lastPoint = new GeoPoint(location.lat, location.lng, location.alt, speed.vN, speed.vE, speed.climb);
        // Update flight plan

        // Notify listeners
        EventBus.getDefault().post(new APLocation(lastPoint));
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }
}
