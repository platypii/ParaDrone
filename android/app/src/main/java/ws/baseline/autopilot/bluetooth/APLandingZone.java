package ws.baseline.autopilot.bluetooth;

public class APLandingZone {
    public final double lat;
    public final double lng;
    public final double alt;
    public final double landingDirection;

    public APLandingZone(double lat, double lng, double alt, double landingDirection) {
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
        this.landingDirection = landingDirection;
    }
}
