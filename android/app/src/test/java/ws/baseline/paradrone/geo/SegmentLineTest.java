package ws.baseline.paradrone.geo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SegmentLineTest {
    private final Point start = new Point(0, 100);
    private final Point end = new Point(100, 0);
    private final SegmentLine line = new SegmentLine(start, end);

    @Test
    public void segmentLineLength() {
        assertEquals(141.4, line.length(), 0.1);
    }

    @Test
    public void segmentLineFly() {
        // Shorten
        assertEquals(50, line.fly(50).length(), 0.1);
        // Extend
        assertEquals(200, line.fly(200).length(), 0.1);
    }

    @Test
    public void segmentLineRender() {
        assertEquals(2, line.render().size());
    }
}
