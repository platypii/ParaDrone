package ws.baseline.autopilot.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.LatLng;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;

public class APLocationEvent implements APEvent {
    public final long millis;
    public final double lat;
    public final double lng;
    public final double alt;

    @Nullable
    public static APLocationEvent lastLocation;

    static void update(long millis, double lat, double lng, double alt) {
        lastLocation = new APLocationEvent(millis, lat, lng, alt);
        EventBus.getDefault().post(lastLocation);
    }

    private APLocationEvent(long millis, double lat, double lng, double alt) {
        this.millis = millis;
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
    }

    public LatLng toLatLng() {
        // TODO: Cache and reuse
        return new LatLng(lat, lng);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%f, %f, %.1f m", lat, lng, alt);
    }
}
