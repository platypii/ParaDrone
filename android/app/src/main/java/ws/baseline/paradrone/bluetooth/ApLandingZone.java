package ws.baseline.paradrone.bluetooth;

import ws.baseline.paradrone.geo.LandingZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.greenrobot.eventbus.EventBus;
import timber.log.Timber;

public class ApLandingZone implements ApEvent {
    @Nullable
    public final LandingZone lz;
    public boolean pending;

    @Nullable
    public static ApLandingZone lastLz;

    private ApLandingZone(@Nullable LandingZone lz, boolean pending) {
        this.lz = lz;
        this.pending = pending;
    }

    public static void setPending(@NonNull LandingZone lz) {
        lastLz = new ApLandingZone(lz, true);
        EventBus.getDefault().post(lastLz);
    }

     public static void parse(@NonNull byte[] value) {
        LandingZone lz = null;
        if (value[0] == 'Z') {
            // 'Z', lat, lng, alt, dir
            final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
            final double lat = buf.getInt(1) * 1e-6; // microdegrees
            final double lng = buf.getInt(5) * 1e-6; // microdegrees
            final double alt = buf.getShort(9) * 0.1; // decimeters
            final double dir = buf.getShort(11) * 0.001; // milliradians
            if (lat != 0 && lng != 0) {
                lz = new LandingZone(lat, lng, alt, dir);
                Timber.i("ap -> phone: lz %s", lz);
            } else {
                lz = null;
                Timber.i("ap -> phone: no lz");
            }
        } else {
            Timber.e("Unexpected landing zone message: %s", value[0]);
        }
        lastLz = new ApLandingZone(lz, false);
        EventBus.getDefault().post(lastLz);
    }
}