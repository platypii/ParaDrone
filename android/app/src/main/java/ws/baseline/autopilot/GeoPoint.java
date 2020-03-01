package ws.baseline.autopilot;

import com.google.android.gms.maps.model.LatLng;

public class GeoPoint {
    public double lat;
    public double lng;
    public double alt;
    public double vN;
    public double vE;
    public double climb;

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
