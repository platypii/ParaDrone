package ws.baseline.paradrone.util;

import androidx.annotation.NonNull;

public class Numbers {

    public static boolean isReal(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    /**
     * Faster than Math.hypot
     */
    public static double hypot(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Linear interpolation
     */
    public static double interpolate(double start, double end, double alpha) {
        return start + alpha * (end - start);
    }

    public static double parseDistance(@NonNull String str) {
        if (str.isEmpty()) {
            return Double.NaN;
        } else {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
    }
}
