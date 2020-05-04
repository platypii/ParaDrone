package ws.baseline.autopilot;

import com.google.android.gms.maps.model.LatLng;

public class GeoPoint {
    public final double lat;
    public final double lng;
    public final double alt;
    public final double vN;
    public final double vE;
    public final double climb;

    public GeoPoint(double lat, double lng, double alt, double vN, double vE, double climb) {
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
        this.vN = vN;
        this.vE = vE;
        this.climb = climb;
    }

    public LatLng toLatLng() {
        // TODO: Cache and reuse
        return new LatLng(lat, lng);
    }
}
