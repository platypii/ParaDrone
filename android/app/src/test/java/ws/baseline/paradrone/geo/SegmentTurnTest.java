package ws.baseline.paradrone.geo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SegmentTurnTest {
    private final Circle circle = new Circle(0, 0, 100);
    private final Point start = new Point(0, 100);
    private final Point end = new Point(100, 0);
    private final SegmentTurn turn = new SegmentTurn(circle, start, end, Turn.TURN_RIGHT);

    @Test
    public void segmentTurnLength() {
        assertEquals(157, turn.length(), 0.1);
    }

    @Test
    public void segmentTurnFly() {
        // Shorten
        assertEquals(50, turn.fly(50).length(), 0.1);
        // Extend
        assertEquals(200, turn.fly(200).length(), 0.1);
    }

    @Test
    public void segmentTurnRender() {
        assertEquals(17, turn.render().size());
    }
}
