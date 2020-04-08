package ws.baseline.autopilot;

public class Paramotor {

    // Constants
    public static final double turnRadius = 143; // Turn radius in meters with 1 toggle buried
    public static final double descentRate = 5; // Meters of altitude lost per second
    public static final double groundSpeed = 10; // Meters per second

    public static double flightDistanceRemaining(double alt) {
        final double timeToGround = alt / Paramotor.descentRate;
        return groundSpeed * timeToGround;
    }
}
