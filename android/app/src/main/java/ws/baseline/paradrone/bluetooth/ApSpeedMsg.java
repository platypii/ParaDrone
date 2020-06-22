package ws.baseline.paradrone.bluetooth;

import androidx.annotation.NonNull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import timber.log.Timber;

public class ApSpeedMsg implements APEvent {
    public final double lat;
    public final double lng;
    public final double alt;
    public final double vN;
    public final double vE;
    public final double climb;

    static void parse(@NonNull byte[] value) {
        // 'S', lat, lng, alt, vN, vE, climb
        final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
        final double lat = buf.getInt(1) * 1e-6; // microdegrees
        final double lng = buf.getInt(5) * 1e-6; // microdegrees
        final double alt = buf.getShort(9) * 0.1; // decimeters
        final double vN = buf.getShort(11) * 0.01; // cm/s
        final double vE = buf.getShort(13) * 0.01; // cm/s
        final double climb = buf.getShort(15) * 0.01; // cm/s
        final ApSpeedMsg lastSpeed = new ApSpeedMsg(lat, lng, alt, vN, vE, climb);
        Timber.d("ap -> phone: speed %s", lastSpeed);
        EventBus.getDefault().post(lastSpeed);
    }

    private ApSpeedMsg(double lat, double lng, double alt, double vN, double vE, double climb) {
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
        this.vN = vN;
        this.vE = vE;
        this.climb = climb;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "S %f, %f, %f, %f, %f, %.1f", lat, lng, alt, vN, vE, climb);
    }
}
