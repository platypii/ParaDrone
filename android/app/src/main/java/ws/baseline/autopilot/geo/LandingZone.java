package ws.baseline.autopilot.geo;

import ws.baseline.autopilot.GeoPoint;
import ws.baseline.autopilot.Paramotor;
import ws.baseline.autopilot.util.Convert;

import androidx.annotation.NonNull;
import java.util.Locale;

public class LandingZone {
    public final LatLngAlt destination;
    /** Landing direction in radians */
    public final double landingDirection;

    // Ground length of final approach
    public final double finalDistance = 150; // meters

    // Destination, as origin of coordinate system
    public final Point3V dest;

    public LandingZone(double lat, double lng, double alt, double landingDirection) {
        this.destination = new LatLngAlt(lat, lng, alt);
        this.landingDirection = landingDirection;
        this.dest = new Point3V(
                0,
                0,
                destination.alt,
                Math.sin(landingDirection),
                Math.cos(landingDirection),
                0
        );
    }

    /**
     * Landing pattern: start of final approach
     */
    public Point3V startOfFinal() {
        return new Point3V(
                -finalDistance * this.dest.vx,
                -finalDistance * this.dest.vy,
                dest.alt + finalDistance / Paramotor.glide,
                dest.vx,
                dest.vy,
                Paramotor.descentRate
        );
    }

    /**
     * Landing pattern: start of base leg
     */
    public Point3V startOfBase(int turn) {
        return new Point3V(
                -finalDistance * (this.dest.vx - turn * this.dest.vy),
                -finalDistance * (turn * this.dest.vx + this.dest.vy),
                dest.alt + 2 * finalDistance / Paramotor.glide,
                -dest.vx,
                -dest.vy,
                Paramotor.descentRate
        );
    }

    /**
     * Landing pattern: start of downwind leg
     */
    public Point3V startOfDownwind(int turn) {
        return new Point3V(
                finalDistance * turn * this.dest.vy,
                -finalDistance * turn * this.dest.vx,
                dest.alt + 3 * finalDistance / Paramotor.glide,
                -dest.vx,
                -dest.vy,
                Paramotor.descentRate
        );
    }

    /**
     * Convert lat, lng to x, y meters centered at current location
     */
    public Point3V toPoint3V(GeoPoint point) {
        final double bearing = Math.toRadians(Geo.bearing(this.destination.lat, this.destination.lng, point.lat, point.lng));
        final double distance = Geo.distance(this.destination.lat, this.destination.lng, point.lat, point.lng);
        return new Point3V(
                distance * Math.sin(bearing),
                distance * Math.cos(bearing),
                point.alt,
                point.vE,
                point.vN,
                point.climb
        );
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
