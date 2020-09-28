package ws.baseline.paradrone.geo;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import java.util.Locale;

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

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%.6f, %6f, %.0f", lat, lng, alt);
    }
}
