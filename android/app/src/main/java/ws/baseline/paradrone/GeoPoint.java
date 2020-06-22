package ws.baseline.paradrone;

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

    public double bearing() {
        return Math.toDegrees(Math.atan2(vE, vN));
    }

    public double groundSpeed() {
        return Math.sqrt(vN * vN + vE * vE);
    }

    public LatLng toLatLng() {
        // TODO: Cache and reuse
        return new LatLng(lat, lng);
    }
}
