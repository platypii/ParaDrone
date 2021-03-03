package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.PointV;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ws.baseline.paradrone.geo.LandingZone.kpow;

public class ShortestDubinsTest {
    @Test
    public void shortestDubins() {
        final PointV start = new PointV(1000, 1000, 10, 0);
        final Path plan = ShortestDubins.shortestDubins(start, kpow.dest, 100);
        assertNotNull(plan);
        assertEquals("DubinR", plan.name);
        assertEquals(3, plan.segments.size());
        assertEquals(1848.3, plan.length(), 0.1);
    }
}
