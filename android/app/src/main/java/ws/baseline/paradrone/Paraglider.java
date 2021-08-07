package ws.baseline.paradrone;

import ws.baseline.paradrone.geo.Geo;
import ws.baseline.paradrone.geo.GeoPoint;
import ws.baseline.paradrone.geo.LatLng;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Paraglider {

    // Constants
    public final double turnRadius = 40; // Turn radius in meters with 1 toggle buried
    public final double climbRate = -3; // m/s
    public final double groundSpeed = 12; // m/s
    public final double glide = -groundSpeed / climbRate;

    @Nullable
    public GeoPoint loc;
    @NonNull
    private final Toggles toggles = new Toggles();

    /**
     * Return the horizontal distance covered in a given amount of vertical distance
     * @param alt altitude in meters
     */
    public double flightDistanceRemaining(double alt) {
        final double timeToGround = -alt / climbRate;
        return groundSpeed * timeToGround;
    }

    /**
     * Predict where the glider will be in dt seconds.
     * Takes into account position, speed, and toggle position.
     * TODO: Adjust velocities on WSE
     */
    public GeoPoint predict(double dt) {
        final double alpha = 0.5;
        if (loc != null) {
            double groundSpeed = Math.sqrt(loc.vE * loc.vE + loc.vN * loc.vN);

            // Update glider turn (yaw) rate and speed based on toggle position
            // TODO: Special case for straight? Faster?
            final double turnSpeed = toggles.turnSpeed();
            final double turnBalance = toggles.turnBalance();
            groundSpeed += (turnSpeed - groundSpeed) * alpha;
            final double distance = groundSpeed * dt;
            // Air bearing
            final double startBearing = Math.atan2(loc.vE, loc.vN);
            final double endBearing = startBearing + distance * turnBalance / turnRadius;
            // The proof of this is beautiful:
            final double chordBearing = startBearing + distance * turnBalance / turnRadius / 2;
            // Move lat,lng by distance and bearing of flight path relative to wind
            final LatLng prewind = Geo.moveBearing(loc.lat, loc.lng, chordBearing, distance);
            // Adjust velocity
            final double vE = groundSpeed * Math.sin(endBearing);
            final double vN = groundSpeed * Math.cos(endBearing);
            // Adjust altitude
            final double alt = loc.alt + loc.climb * dt;
            final double climb = loc.climb + (climbRate - loc.climb) * alpha;
            return new GeoPoint(prewind.lat, prewind.lng, alt, vN, vE, climb);
        } else {
            return null;
        }
    }
}
