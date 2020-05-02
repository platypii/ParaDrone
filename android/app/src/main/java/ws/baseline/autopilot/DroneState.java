package ws.baseline.autopilot;

import ws.baseline.autopilot.geo.LandingZone;

import androidx.annotation.Nullable;

public class DroneState {
    public final long lastUpdate;
    public final LandingZone lz;
    public final GeoPoint currentLocation;

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

}
