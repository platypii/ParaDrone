package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.GeoPoint;
import ws.baseline.paradrone.bluetooth.APLandingZone;
import ws.baseline.paradrone.bluetooth.ApLocationMsg;
import ws.baseline.paradrone.bluetooth.ApSpeedMsg;
import ws.baseline.paradrone.geo.LandingZone;
import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.Point3V;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class FlightComputer {

    // Combined location and speed data
    @Nullable
    private GeoPoint lastPoint;
    @Nullable
    private LandingZone lastLz;

    @Nullable
    public Path plan;

    public void start() {
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onApLandingZone(@NonNull APLandingZone event) {
        lastLz = event.lz;
        replan();
    }

    @Subscribe
    public void onApLocation(@NonNull ApLocationMsg event) {
        // TODO: Use kalman filter to estimate velocities
        lastPoint = new GeoPoint(event.lat, event.lng, event.alt, Double.NaN, Double.NaN, Double.NaN);
    }

    @Subscribe
    public void onApSpeed(@NonNull ApSpeedMsg event) {
        lastPoint = new GeoPoint(event.lat, event.lng, event.alt, event.vN, event.vE, event.climb);
        replan();
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }

    private void replan() {
        if (lastPoint != null && lastLz != null) {
            final Point3V point = lastLz.toPoint3V(lastPoint);
            // Recompute plan
            plan = Search.search(point, lastLz);
            EventBus.getDefault().post(new PlanEvent());
        }
    }
}
