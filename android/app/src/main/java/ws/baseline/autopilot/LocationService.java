package ws.baseline.autopilot;

import ws.baseline.autopilot.bluetooth.ApLocationMsg;
import ws.baseline.autopilot.bluetooth.ApSpeedMsg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static java.lang.Double.NaN;

public class LocationService {
    /**
     * The last received AP location
     */
    @Nullable
    public GeoPoint lastPoint = null;
    public long lastMillis;

    public void start() {
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onApLocation(@NonNull ApLocationMsg msg) {
        lastPoint = new GeoPoint(msg.lat, msg.lng, msg.alt, NaN, NaN, NaN);
        lastMillis = System.currentTimeMillis();
        // TODO: Kalman filter for speeds
        EventBus.getDefault().post(lastPoint);
    }

    @Subscribe
    public void onApSpeed(@NonNull ApSpeedMsg msg) {
        lastPoint = new GeoPoint(msg.lat, msg.lng, msg.alt, msg.vN, msg.vE, msg.climb);
        lastMillis = System.currentTimeMillis();
        EventBus.getDefault().post(lastPoint);
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }
}
