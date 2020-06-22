package ws.baseline.paradrone.geo;

import androidx.annotation.NonNull;
import java.util.Locale;

public class LatLng {
    public final double lat;
    public final double lng;

    public LatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public com.google.android.gms.maps.model.LatLng toLatLng() {
        // TODO: Cache and reuse
        return new com.google.android.gms.maps.model.LatLng(lat, lng);
    }


    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%f, %f", lat, lng);
    }
}
