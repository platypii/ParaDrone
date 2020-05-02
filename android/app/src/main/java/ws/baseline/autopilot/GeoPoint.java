package ws.baseline.autopilot;

import com.google.android.gms.maps.model.LatLng;

public class GeoPoint {
    public double lat;
    public double lng;
    public double alt;
    public double vN;
    public double vE;
    public double climb;

    public LatLng toLatLng() {
        // TODO: Cache and reuse
        return new LatLng(lat, lng);
    }
}
