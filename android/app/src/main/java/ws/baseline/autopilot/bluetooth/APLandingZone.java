package ws.baseline.autopilot.bluetooth;

import ws.baseline.autopilot.geo.LandingZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;

public class APLandingZone implements APEvent {
    @NonNull
    public final LandingZone lz;
    public final boolean pending;

    @Nullable
    public static APLandingZone lastLz;

    private APLandingZone(@NonNull LandingZone lz, boolean pending) {
        this.lz = lz;
        this.pending = pending;
    }

    public static void update(@NonNull LandingZone lz, boolean pending) {
        lastLz = new APLandingZone(lz, pending);
        EventBus.getDefault().post(lastLz);
    }
}
