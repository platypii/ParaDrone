package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.ApLocationMsg;
import ws.baseline.paradrone.bluetooth.ApSpeedMsg;
import ws.baseline.paradrone.util.RefreshRateEstimator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import timber.log.Timber;

import static java.lang.Double.NaN;

public class LocationService {
    /**
     * The last received AP location
     */
    @Nullable
    public GeoPoint lastLoc = null;
    public long lastMillis = -1;

    // Moving average of refresh rate in Hz
    public final RefreshRateEstimator refreshRate = new RefreshRateEstimator();

    public void start() {
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onApLocation(@NonNull ApLocationMsg msg) {
        lastLoc = new GeoPoint(msg.lat, msg.lng, msg.alt, NaN, NaN, NaN);
        lastMillis = System.currentTimeMillis();
        refreshRate.addSample(lastMillis);
        // TODO: Kalman filter for speeds
        EventBus.getDefault().post(lastLoc);
    }

    @Subscribe
    public void onApSpeed(@NonNull ApSpeedMsg msg) {
        lastLoc = new GeoPoint(msg.lat, msg.lng, msg.alt, msg.vN, msg.vE, msg.climb);
        lastMillis = System.currentTimeMillis();
        refreshRate.addSample(lastMillis);
        EventBus.getDefault().post(lastLoc);
    }

    /**
     * Returns the number of milliseconds since the last fix
     */
    public long lastFixDuration() {
        if (lastLoc != null && lastMillis > 0) {
            final long duration = System.currentTimeMillis() - lastMillis;
            if (duration < 0) {
                Timber.w("Time since last fix should never be negative delta = " + duration + "ms");
            }
            return duration;
        } else {
            return -1;
        }
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }
}
