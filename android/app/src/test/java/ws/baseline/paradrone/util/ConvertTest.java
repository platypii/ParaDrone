package ws.baseline.paradrone.util;

import org.junit.Test;

import static java.lang.Math.toRadians;
import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are converting correctly
 */
public class ConvertTest {

    @Test
    public void convertDistance() {
        Convert.metric = false;
        assertEquals("0 ft", Convert.distance(0.0));
        assertEquals("3 ft", Convert.distance(1.0));
        assertEquals("3.3 ft", Convert.distance(1.0, 1, true));
        assertEquals("3.3", Convert.distance(1.0, 1, false));
        assertEquals("", Convert.distance(Double.NaN));
        assertEquals("Infinity", Convert.distance(Double.POSITIVE_INFINITY));
    }

    @Test
    public void convertDistance3() {
        Convert.metric = false;
        assertEquals("3 ft", Convert.distance3(1));
        assertEquals("3281 ft", Convert.distance3(1000));
        assertEquals("1.0 mi", Convert.distance3(1609.34));
        assertEquals("10 mi", Convert.distance3(16093.4));
        assertEquals("", Convert.distance3(Double.NaN));
        assertEquals("Infinity", Convert.distance3(Double.POSITIVE_INFINITY));
        assertEquals("-Infinity", Convert.distance3(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void convertDistance3Metric() {
        Convert.metric = true;
        assertEquals("1 m", Convert.distance3(1));
        assertEquals("1.0 km", Convert.distance3(1000));
        assertEquals("1.6 km", Convert.distance3(1609.34));
        assertEquals("16 km", Convert.distance3(16093.4));
        assertEquals("161 km", Convert.distance3(160934));
    }

    @Test
    public void convertSpeed() {
        Convert.metric = false;
        assertEquals("0.0 mph", Convert.speed(0.0));
        assertEquals("9.9 mph", Convert.speed(9.9 * Convert.MPH));
        assertEquals("22 mph", Convert.speed(10.0));
        assertEquals("224 mph", Convert.speed(100.0));
        assertEquals("224 mph", Convert.speed(100.0, 0, true));
        assertEquals("224", Convert.speed(100.0, 0, false));
        assertEquals("", Convert.speed(Double.NaN));
        assertEquals("Infinity", Convert.speed(Double.POSITIVE_INFINITY));
    }

    @Test
    public void convertbearing3() {
        assertEquals("N", Convert.bearing3(toRadians(0.0)));
        assertEquals("NE", Convert.bearing3(toRadians(45.0)));
        assertEquals("E", Convert.bearing3(toRadians(90.0)));
        assertEquals("E", Convert.bearing3(toRadians(90.5)));
        assertEquals("SE", Convert.bearing3(toRadians(135.0)));
        assertEquals("S", Convert.bearing3(toRadians(188.0)));
        assertEquals("SW", Convert.bearing3(toRadians(-135.0)));
        assertEquals("W", Convert.bearing3(toRadians(-90.0)));
        assertEquals("NW", Convert.bearing3(toRadians(660.0)));
        assertEquals("NW", Convert.bearing3(toRadians(1020.0)));
        assertEquals("NW", Convert.bearing3(toRadians(-60.0)));
        assertEquals("NW", Convert.bearing3(toRadians(-420.0)));
        assertEquals("NW", Convert.bearing3(toRadians(-780.0)));
        assertEquals("", Convert.bearing3(Double.NaN));
    }
}
