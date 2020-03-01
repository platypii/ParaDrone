package ws.baseline.autopilot;

import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;

public class DroneState {
    public long lastUpdate;
    public LandingZone lz;
    public GeoPoint currentLocation;

    @Nullable
    private static DroneState state;

    private DroneState(long lastUpdate, LandingZone lz, GeoPoint currentLocation) {
        this.lastUpdate = lastUpdate;
        this.lz = lz;
        this.currentLocation = currentLocation;
    }

    @Nullable
    public static DroneState get() {
        return state;
    }

    public static void set(DroneState state) {
        DroneState.state = state;
        EventBus.getDefault().post(state);
    }

    // updated, lzLat, lzLng, lzAlt, lzDir, currLat, currLng, currAlt
    public static DroneState parse(String line) {
        final String[] split = line.split(",");
        final long lastUpdate = Long.parseLong(split[0]);
        final double lzLat = Double.parseDouble(split[1]);
        final double lzLng = Double.parseDouble(split[2]);
        final double lzAlt = Double.parseDouble(split[3]);
        final double lzDir = Double.parseDouble(split[4]);

        return new DroneState(lastUpdate, new LandingZone(), new GeoPoint());
    }

}
