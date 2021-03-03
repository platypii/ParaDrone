package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.Paraglider;
import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.Point3V;

import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static ws.baseline.paradrone.geo.LandingZone.kpow;

public class PlannerWaypointsTest {
    private final Paraglider para = new Paraglider();
    private final LandingPattern pattern = new LandingPattern(para, kpow);

    @Test
    public void waypointsPlanner() {
        final Point3V start = new Point3V(1000, 1000, 800, 10, 0, -3);
        final List<Path> plans = PlannerWaypoints.viaWaypoints(start, pattern, 100);
        assertEquals(8, plans.size());
        assertEquals(12, plans.get(0).segments.size());
        assertEquals(9, plans.get(1).segments.size());
        assertEquals(6, plans.get(2).segments.size());
        assertEquals(3, plans.get(3).segments.size());
        assertEquals("Waypoint", plans.get(0).name);
        assertEquals(2979.3, plans.get(0).length(), 0.1);
        assertEquals(2793.1, plans.get(1).length(), 0.1);
        assertEquals(2778.8, plans.get(2).length(), 0.1);
        assertEquals(1995.5, plans.get(3).length(), 0.1);
    }
}
