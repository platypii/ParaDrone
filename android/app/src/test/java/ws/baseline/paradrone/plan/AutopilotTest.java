package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.GeoPoint;
import ws.baseline.paradrone.Paraglider;
import ws.baseline.paradrone.geo.LandingZone;
import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.Point3V;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AutopilotTest {
    private final LandingZone kpow = new LandingZone(47.239, -123.143, 84, Math.toRadians(32));

    @Test
    public void autopilotFar() {
        final Paraglider para = new Paraglider();
        final Point3V start = new Point3V(1000, 1000, 800, 10, 0, -5);
        para.loc = kpow.toGeoPoint(start);
        final Path plan = Autopilot.search(para, kpow);
        assertNotNull(plan);
        assertEquals("naive", plan.name);
        assertEquals(2, plan.segments.size());
        assertEquals(3580, plan.length(), 0.1);
    }

    @Test
    public void autopilotNear() {
        final Paraglider para = new Paraglider();
        para.loc = new GeoPoint(47.24, -123.14, 884, 0, 10, -5);
        final Path plan = Autopilot.search(para, kpow);
        assertNotNull(plan);
        assertEquals("waypoints", plan.name);
        assertEquals(13, plan.segments.size());
        assertEquals(4000, plan.length(), 0.1);
    }
}
