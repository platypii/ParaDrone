package ws.baseline.autopilot.bluetooth;

import ws.baseline.autopilot.geo.LandingZone;

import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;

public class APLandingZone implements APEvent {
    public final LandingZone lz;

    @Nullable
    public static APLandingZone lastLz;

    public APLandingZone(LandingZone lz) {
        this.lz = lz;
    }

    public static void update(LandingZone lz) {
        lastLz = new APLandingZone(lz);
        EventBus.getDefault().post(lastLz);
    }
}
