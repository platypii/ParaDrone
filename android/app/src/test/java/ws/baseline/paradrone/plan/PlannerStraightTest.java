package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.PointV;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PlannerStraightTest {
    @Test
    public void straightPlanner() {
        final PointV start = new PointV(1000, 1000, 10, 0);
        final Path plan = PlannerStraight.straight(start);
        assertNotNull(plan);
        assertEquals("straight", plan.name);
        assertEquals(1, plan.segments.size());
        assertEquals(10, plan.length(), 0.1);
    }
}
