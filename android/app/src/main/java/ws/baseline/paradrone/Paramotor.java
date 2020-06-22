package ws.baseline.paradrone;

public class Paramotor {

    // Constants
    public static final double turnRadius = 143; // Turn radius in meters with 1 toggle buried
    public static final double descentRate = 3; // Meters of altitude lost per second
    public static final double groundSpeed = 15; // Meters per second
    public static final double glide = groundSpeed / descentRate;

    public static double flightDistanceRemaining(double alt) {
        // TODO: Remove height from which we will always fly straight to avoid low turns
        final double timeToGround = alt / Paramotor.descentRate;
        return groundSpeed * timeToGround;
    }
}
