package ws.baseline.paradrone.geo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are parsing NMEA correctly
 */
public class GeoTest {

    private final LatLng seattle = new LatLng(47.60, -122.33);
    private final LatLng la = new LatLng(34.0, -118.2);

    private final double bearing = 2.8913; // radians
    private final double distance = 1551093.52;

    @Test
    public void bearing() {
        assertEquals(bearing, Geo.bearing(seattle.lat, seattle.lng, la.lat, la.lng), 0.01);
    }

    @Test
    public void distance() {
        assertEquals(distance, Geo.distance(seattle.lat, seattle.lng, la.lat, la.lng), 0.01);
    }

    @Test
    public void fastDistance() {
        // Allow 0.1% error
        assertEquals(distance, Geo.fastDistance(seattle.lat, seattle.lng, la.lat, la.lng), 0.001 * distance);
    }

    @Test
    public void moveDirection() {
        final LatLng moved = Geo.moveDirection(seattle.lat, seattle.lng, bearing, distance);
        assertEquals(la.lat, moved.lat, 0.01);
        assertEquals(la.lng, moved.lng, 0.01);
    }

}
