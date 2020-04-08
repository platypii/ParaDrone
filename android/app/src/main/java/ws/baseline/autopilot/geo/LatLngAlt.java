package ws.baseline.autopilot.geo;

import com.google.android.gms.maps.model.LatLng;

public class LatLngAlt {
    public final double lat;
    public final double lng;
    public final double alt;

    public LatLngAlt(double lat, double lng, double alt) {
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
    }

    public LatLng toLatLng() {
        // TODO: Cache and reuse
        return new LatLng(lat, lng);
    }
}
