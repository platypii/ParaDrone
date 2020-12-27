package ws.baseline.paradrone.geo;

import ws.baseline.paradrone.GeoPoint;
import ws.baseline.paradrone.util.Convert;

import androidx.annotation.NonNull;
import java.util.Locale;

public class LandingZone {
    public static final LandingZone kpow = new LandingZone(47.239, -123.143, 84, Math.toRadians(32));

    @NonNull
    public final LatLngAlt destination;
    /** Landing direction in radians */
    public final double landingDirection;

    // Ground length of final approach
    public final double finalDistance = 150; // meters

    // Destination, as origin of coordinate system
    @NonNull
    public final Point3V dest;

    /**
     * Construct a LandingZone object.
     *
     * @param lat latitude in decimal degrees
     * @param lng longitude in decimal degrees
     * @param alt altitude msl in meters
     * @param landingDirection direction to be facing on landing in radians
     */
    public LandingZone(double lat, double lng, double alt, double landingDirection) {
        this.destination = new LatLngAlt(lat, lng, alt);
        this.landingDirection = landingDirection;
        this.dest = new Point3V(
                0,
                0,
                0,
                Math.sin(landingDirection),
                Math.cos(landingDirection),
                0
        );
    }

    /**
     * Convert lat, lng to x, y meters centered at current location
     */
    @NonNull
    public Point3V toPoint3V(@NonNull GeoPoint point) {
        final double bearing = Geo.bearing(this.destination.lat, this.destination.lng, point.lat, point.lng);
        final double distance = Geo.distance(this.destination.lat, this.destination.lng, point.lat, point.lng);
        return new Point3V(
                distance * Math.sin(bearing),
                distance * Math.cos(bearing),
                point.alt - destination.alt,
                point.vE,
                point.vN,
                point.climb
        );
    }

    /**
     * Convert x, y coordinates to lat, lng
     */
    @NonNull
    public LatLng toLatLng(@NonNull Point point) {
        final double bearing = Math.atan2(point.x, point.y);
        final double distance = Math.sqrt(point.x * point.x + point.y * point.y);
        return Geo.moveBearing(this.destination.lat, this.destination.lng, bearing, distance);
    }

    /**
     * Convert Point3V to GeoPointV
     */
    @NonNull
    public GeoPoint toGeoPoint(@NonNull Point3V point) {
        final LatLng ll = toLatLng(point);
        return new GeoPoint(ll.lat, ll.lng, point.alt, point.vy, point.vx, point.climb);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%.6f, %.6f, %s, %.0f°", destination.lat, destination.lng, Convert.distance(destination.alt), Math.toDegrees(landingDirection));
    }
}
