package ws.baseline.paradrone;

import androidx.annotation.Nullable;

public class Paraglider {

    // Constants
    public final double turnRadius = 143; // Turn radius in meters with 1 toggle buried
    public final double climbRate = -3; // Meters of altitude lost per second
    public final double groundSpeed = 15; // Meters per second
    public final double glide = -groundSpeed / climbRate;

    @Nullable
    public GeoPoint loc;

    /**
     * Return the horizontal distance covered in a given amount of vertical distance
     * @param alt altitude in meters
     */
    public double flightDistanceRemaining(double alt) {
        final double timeToGround = -alt / climbRate;
        return groundSpeed * timeToGround;
    }
}
