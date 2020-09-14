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

    public static double parseDistance(String str) {
        if (str.endsWith(" m")) {
            return Double.parseDouble(str.substring(0, str.length() - 2));
        } else {
            return Double.NaN;
        }
    }
}
