package ws.baseline.autopilot.plan;

import ws.baseline.autopilot.GeoPoint;
import ws.baseline.autopilot.bluetooth.APLandingZone;
import ws.baseline.autopilot.bluetooth.APLocationMsg;
import ws.baseline.autopilot.bluetooth.APSpeedMsg;
import ws.baseline.autopilot.geo.LandingZone;
import ws.baseline.autopilot.geo.Path;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import timber.log.Timber;

public class FlightComputer {

    // Combined location and speed data
    @Nullable
    private GeoPoint lastPoint;
    @Nullable
    private LandingZone lastLz;

    @Nullable
    public Path plan;

    // Raw bluetooth messages
    private APLocationMsg location;

    public void start() {
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onApLandingZone(@NonNull APLandingZone event) {
        lastLz = event.lz;
        replan();
    }

    @Subscribe
    public void onApLocation(@NonNull APLocationMsg event) {
        this.location = event;
    }

    @Subscribe
    public void onApSpeed(@NonNull APSpeedMsg speed) {
        if (location.millis != speed.millis) {
            Timber.e("Location/speed mismatch " + location.millis + " != " + speed.millis);
        }
        // Combine location and speed data
        lastPoint = new GeoPoint(location.lat, location.lng, location.alt, speed.vN, speed.vE, speed.climb);
        replan();
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }

    private void replan() {
        if (lastPoint != null && lastLz != null) {
            // Recompute plan
            plan = Search.search(lastPoint, lastLz);
            EventBus.getDefault().post(new PlanEvent());
        }
    }
}
