package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.geo.GeoPoint;
import ws.baseline.paradrone.Paraglider;
import ws.baseline.paradrone.bluetooth.ApLandingZone;
import ws.baseline.paradrone.bluetooth.ApLocationMsg;
import ws.baseline.paradrone.bluetooth.ApSpeedMsg;
import ws.baseline.paradrone.geo.LandingZone;
import ws.baseline.paradrone.geo.Path;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class FlightComputer {

    @NonNull
    private final Paraglider para = new Paraglider();

    @Nullable
    private LandingZone lastLz;

    @Nullable
    public Path plan;

    public void start() {
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onApLandingZone(@NonNull ApLandingZone event) {
        lastLz = event.lz;
        replan();
    }

    @Subscribe
    public void onApLocation(@NonNull ApLocationMsg event) {
        // TODO: Use kalman filter to estimate velocities
        para.loc = new GeoPoint(event.lat, event.lng, event.alt, Double.NaN, Double.NaN, Double.NaN);
        replan();
    }

    @Subscribe
    public void onApSpeed(@NonNull ApSpeedMsg event) {
        para.loc = new GeoPoint(event.lat, event.lng, event.alt, event.vN, event.vE, event.climb);
        replan();
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }

    private void replan() {
        if (para.loc != null && lastLz != null) {
            // Recompute plan
            plan = Autopilot.search3(para, lastLz);
            EventBus.getDefault().post(new PlanEvent());
        }
    }
}
