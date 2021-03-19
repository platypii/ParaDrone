package ws.baseline.paradrone.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

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

    public static int parseInt(@Nullable String str, int defaultValue) {
        if (str == null || str.isEmpty()) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                Timber.e(e);
                return defaultValue;
            }
        }
    }
}
