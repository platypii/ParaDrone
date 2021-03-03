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
    private final Paraglider para = new Paraglider();

    @Test
    public void autopilotFar() {
        final Point3V far = new Point3V(1000, 1000, 800, 10, 0, -3);
        final Path plan = Autopilot.search(far, para, kpow);
        assertNotNull(plan);
        final double score = Math.hypot(plan.end().x, plan.end().y);
        assertEquals("NaiveR", plan.name);
        assertEquals(2, plan.segments.size());
        assertEquals(3200, plan.length(), 0.1);
        assertEquals(1701.7, score, 0.1);
    }

    @Test
    public void autopilotNear() {
        final GeoPoint ll = new GeoPoint(47.24, -123.14, 884, 0, -10, -3);
        final Point3V near = kpow.toPoint3V(ll);
        final Path plan = Autopilot.search(near, para, kpow);
        assertNotNull(plan);
        final double score = Math.hypot(plan.end().x, plan.end().y);
        assertEquals("Waypoint", plan.name);
        assertEquals(13, plan.segments.size());
        assertEquals(3200, plan.length(), 0.1);
        assertEquals(2330.9, score, 0.1);
    }
}
