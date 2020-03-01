package ws.baseline.autopilot.util;

import androidx.annotation.NonNull;
import java.util.Locale;

/**
 * General conversion utility class
 * Internally we always use metric units (meters, m/s, etc)
 * Technically the metric unit for angles should be radians. We use degrees.
 */
public class Convert {

    public static boolean metric = metricDefault();

    // Convert to standard metric (1000 * FT = 304.8 * M)
    public static final double FT = 0.3048;
    public static final double MPH = 0.44704;
    public static final double KPH = 0.277778;
    private static final double MILE = 1609.34;

    /**
     * Convert meters to local units
     *
     * @param m meters
     * @return distance string in local units
     */
    @NonNull
    public static String distance(double m) {
        return distance(m, 0, true);
    }

    /**
     * Convert meters to local units
     *
     * @param m meters
     * @param precision number of decimal places
     * @param units show the units?
     * @return distance string in local units
     */
    @NonNull
    public static String distance(double m, int precision, boolean units) {
        if (Double.isNaN(m)) {
            return "";
        } else if (Double.isInfinite(m)) {
            return Double.toString(m);
        } else {
            final String unitString = units ? (metric ? " m" : " ft") : "";
            final double localValue = metric ? m : m * 3.2808399;
            if (precision == 0) {
                // Faster special case for integers
                return Math.round(localValue) + unitString;
            } else {
                return String.format("%." + precision + "f%s", localValue, unitString);
            }
        }
    }

    /**
     * Shortened distance intended to be displayed, with units
     *
     * @param m meters
     */
    @NonNull
    public static String distance3(double m) {
        if (Double.isNaN(m)) {
            return "";
        } else if (Double.isInfinite(m)) {
            return Double.toString(m);
        } else {
            if (metric) {
                if (m >= 1000) {
                    return Math.round(m * 0.001) + " km";
                } else {
                    return Math.round(m) + " m";
                }
            } else {
                if (m >= MILE) {
                    // Need max because of float error
                    final double miles = Math.max(1, m * 0.000621371192);
                    return Math.round(miles) + " mi";
                } else {
                    return Math.round(m * 3.2808399) + " ft";
                }
            }
        }
    }

    /**
     * Convert meters/second to local units
     *
     * @param mps meters per second
     * @return speed string in local units
     */
    @NonNull
    public static String speed(double mps) {
        final double smallMps = metric ? 10 * KPH : 10 * MPH;
        if (mps < smallMps) {
            return speed(mps, 1, true);
        } else {
            return speed(mps, 0, true);
        }
    }

    /**
     * Convert meters/second to local units
     *
     * @param mps meters per second
     * @param precision number of decimal places
     * @param units show the units?
     * @return speed string in local units
     */
    @NonNull
    public static String speed(double mps, int precision, boolean units) {
        if (Double.isNaN(mps)) {
            return "";
        } else if (Double.isInfinite(mps)) {
            return Double.toString(mps);
        } else {
            final String unitString = units ? (metric ? " km/h" : " mph") : "";
            final double localValue = metric ? mps * 3.6 : mps * 2.23693629;
            if (precision == 0) {
                // Faster special case for integers
                return Math.round(localValue) + unitString;
            } else {
                return String.format("%." + precision + "f%s", localValue, unitString);
            }
        }
    }

    /**
     * Returns true if the system default locale indicates metric
     */
    private static boolean metricDefault() {
        final String country = Locale.getDefault().getCountry();
        // Everyone except 'merica
        return !"US".equals(country);
    }

}
