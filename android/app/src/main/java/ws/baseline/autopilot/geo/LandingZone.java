package ws.baseline.autopilot.geo;

import ws.baseline.autopilot.GeoPoint;
import ws.baseline.autopilot.util.Convert;

import androidx.annotation.NonNull;
import java.util.Locale;

public class LandingZone {
    public final LatLngAlt destination;
    public final double landingDirection; // radians

    // Ground length of final approach
    public final double finalDistance = 100; // meters

    // Destination, as origin of coordinate system
    public final PointV dest;

    public LandingZone(double lat, double lng, double alt, double landingDirection) {
        this.destination = new LatLngAlt(lat, lng, alt);
        this.landingDirection = landingDirection;
        this.dest = new PointV(
                0,
                0,
                Math.cos(this.landingDirection),
                Math.sin(this.landingDirection)
        );
    }

    /**
     * Landing pattern: start of final approach
     */
    public PointV startOfFinal() {
        return new PointV(
                -finalDistance * this.dest.vx,
                -finalDistance * this.dest.vy,
                dest.vx,
                dest.vy
        );
    }

    /**
     * Landing pattern: start of base leg
     */
    Point startOfBase(int turn) {
        return new Point(
                -finalDistance * (this.dest.vx - turn * this.dest.vy),
                -finalDistance * (turn * this.dest.vx + this.dest.vy)
        );
    }

    /**
     * Landing pattern: start of downwind leg
     */
    Point startOfDownwind(int turn) {
        return new Point(
                finalDistance * turn * this.dest.vy,
                -finalDistance * turn * this.dest.vx
        );
    }

    /**
     * Convert lat, lng to x, y meters centered at current location
     */
    public PointV toPointV(GeoPoint point) {
        final double bearing = Math.toRadians(Geo.bearing(this.destination.lat, this.destination.lng, point.lat, point.lng));
        final double distance = Geo.distance(this.destination.lat, this.destination.lng, point.lat, point.lng);
        return new PointV(distance * Math.sin(bearing),distance * Math.cos(bearing), point.vE, point.vN);
    }

    /**
     * Convert x, y coordinates to lat, lng
     */
    public LatLng toLatLng(Point point) {
        final double bear = Math.toDegrees(Math.atan2(point.x, point.y));
        final double dist = Math.sqrt(point.x * point.x + point.y * point.y);
        return Geo.moveDirection(this.destination.lat, this.destination.lng, bear, dist);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%.6f, %.6f, %s, %.0f°", destination.lat, destination.lng, Convert.distance(destination.alt), Math.toDegrees(landingDirection));
    }
}
