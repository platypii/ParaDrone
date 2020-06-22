package ws.baseline.paradrone.util;

public class Numbers {

    public static boolean isReal(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    /**
     * Linear interpolation
     */
    public static double interpolate(double start, double end, double alpha) {
        return start + alpha * (end - start);
    }

}
