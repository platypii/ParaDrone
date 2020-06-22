package ws.baseline.paradrone.geo;

import java.util.Arrays;
import java.util.List;
import timber.log.Timber;

import static ws.baseline.paradrone.util.Numbers.interpolate;

public class SegmentLine implements Segment {

    private final PointV start;
    private final PointV end;

    public SegmentLine(Point start, Point end) {
        final double dx = end.x - start.x;
        final double dy = end.x - start.y;
        final double len = Math.hypot(dx, dy);
        this.start = new PointV(start.x, start.y, dx / len, dy / len);
        this.end = new PointV(end.x, end.y, dx / len, dy / len);
        if (len == 0) {
            Timber.e("Invalid line: same start and end %s", start);
        }
    }

    @Override
    public PointV start() {
        return start;
    }

    @Override
    public PointV end() {
        return end;
    }

    @Override
    public Path fly(double distance) {
        if (distance < 0) {
            Timber.e("Flight distance cannot be negative %s", distance);
        }
        // Linear interpolate
        final double alpha = distance / this.length();
        final Point proj = new Point(
                interpolate(this.start.x, this.end.x, alpha),
                interpolate(this.start.y, this.end.y, alpha)
        );
        return new Path("line-fly", new SegmentLine(start, proj));
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
