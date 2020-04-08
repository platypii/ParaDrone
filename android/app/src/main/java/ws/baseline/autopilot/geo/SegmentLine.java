package ws.baseline.autopilot.geo;

import java.util.Arrays;
import java.util.List;

import static ws.baseline.autopilot.util.Numbers.interpolate;

public class SegmentLine implements Segment {
    private final Point start;
    private final Point end;

    public SegmentLine(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Point start() {
        return start;
    }

    @Override
    public Point end() {
        return end;
    }

    @Override
    public PathLike fly(double distance) {
        // Linear interpolate
        final double alpha = distance / this.length();
        final Point proj = new Point(
                interpolate(this.start.x, this.end.x, alpha),
                interpolate(this.start.y, this.end.y, alpha)
        );
        return new SegmentLine(start, proj);
    }

    @Override
    public double length() {
        final double dx = start.x - end.x;
        final double dy = start.y - end.y;
        return Math.hypot(dx, dy);
    }

    @Override
    public List<Point> render() {
        return Arrays.asList(start, end);
    }
}
