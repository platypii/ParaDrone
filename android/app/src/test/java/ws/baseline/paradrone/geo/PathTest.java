package ws.baseline.paradrone.geo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PathTest {
    private final Circle circle = new Circle(0, 0, 100);
    private final Point start1 = new Point(0, 100);
    private final Point end1 = new Point(100, 0);
    private final SegmentTurn turn = new SegmentTurn(circle, start1, end1, Turn.TURN_RIGHT);
    private final Point start2 = new Point(100, 0);
    private final Point end2 = new Point(0, 0);
    private final SegmentLine line = new SegmentLine(start2, end2);
    private final Path path = new Path("test-path", turn, line);

    @Test
    public void pathLength() {
        assertEquals(257, path.length(), 0.1);
    }

    @Test
    public void pathFly() {
        // Shorten
        assertEquals(50, path.fly(50).length(), 0.1);
        // Extend
        assertEquals(200, path.fly(200).length(), 0.1);
    }

    @Test
    public void pathRender() {
        assertEquals(19, path.render().size());
    }
}
