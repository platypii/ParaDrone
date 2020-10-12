package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.PointV;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PlannerNaiveTest {
    @Test
    public void naivePlanner() {
        final PointV start = new PointV(1000, 1000, 10, 0);
        final PointV end = new PointV(0, 0, 10, 0);
        final Path plan = PlannerNaive.naive(start, end, 100);
        assertNotNull(plan);
        assertEquals("naive", plan.name);
        assertEquals(2, plan.segments.size());
        assertEquals(1590, plan.length(), 0.1);
    }
}
