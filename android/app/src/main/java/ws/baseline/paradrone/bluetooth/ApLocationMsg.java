package ws.baseline.paradrone.bluetooth;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import timber.log.Timber;

public class ApLocationMsg implements ApEvent {
    public final double lat;
    public final double lng;
    public final double alt;

    static void parse(@NonNull byte[] value) {
        // 'L', lat, lng, alt
        final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
        final double lat = buf.getInt(1) * 1e-6; // microdegrees
        final double lng = buf.getInt(5) * 1e-6; // microdegrees
        final double alt = buf.getShort(9) * 0.1; // decimeters

        if (LocationCheck.validate(lat, lng) == 0) {
            Timber.d("ap -> phone: location " + lat + " " + lng + " " + alt);
            final ApLocationMsg lastLocation = new ApLocationMsg(lat, lng, alt);
            EventBus.getDefault().post(lastLocation);
        } else {
            Timber.w("ap -> phone: invalid location %f, %f", lat, lng);
        }
    }

    private ApLocationMsg(double lat, double lng, double alt) {
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
        return String.format(Locale.getDefault(), "L %f, %f, %.1f m", lat, lng, alt);
    }
}
