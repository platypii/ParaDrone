package ws.baseline.autopilot;

import com.google.android.gms.maps.model.LatLng;

public class LatLngAlt {
    public double lat;
    public double lng;
    public double alt;

    public LatLng toLatLng() {
        // TODO: Cache and reuse
        return new LatLng(lat, lng);
    }
}
