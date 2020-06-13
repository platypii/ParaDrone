package ws.baseline.autopilot.geo;

import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class SegmentTurn implements Segment {

    private final Circle circle;
    private final PointV start;
    private final PointV end;
    private final int turn;

    public SegmentTurn(Circle circle, Point start, Point end, int turn) {
        this.circle = circle;
        final double start_dx = start.x - circle.x;
        final double start_dy = start.y - circle.y;
        this.start = new PointV(start.x, start.y, start_dx / circle.radius, start_dy / circle.radius);
        final double end_dx = end.x - circle.x;
        final double end_dy = end.y - circle.y;
        this.end = new PointV(end.x, end.y, end_dx / circle.radius, end_dy / circle.radius);
        this.turn = turn;
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
        final double len = length();
        if (distance < len) {
            final double theta = angle1() + turn * distance / circle.radius;
            final Point end = new Point(
                    circle.x + circle.radius * Math.sin(theta),
                    circle.y + circle.radius * Math.cos(theta)
            );
            return new Path("turn-fly", new SegmentTurn(circle, start, end, turn));
        } else {
            final double remaining = distance - len;
            final double dx = end.x - circle.x;
            final double dy = end.y - circle.y;
            final Point proj = new Point(
                    end.x + turn * remaining * dy / circle.radius,
                    end.y - turn * remaining * dx / circle.radius
            );
            final SegmentLine line = new SegmentLine(end, proj);
            return new Path("turn-fly", this, line);
        }
    }

    @Override
    public double length() {
        return circle.radius * arcs();
    }

    @Override
    public List<Point> render() {
        final List<Point> points = new ArrayList<>();
        final double angle1 = angle1();
        final double step = 0.1; // ~5 degrees
        final double arcs = arcs();
        for (double delta = 0; delta < arcs; delta += step) {
            final double theta = angle1 + turn * delta;
            points.add(new Point(
                    circle.x + circle.radius * Math.sin(theta),
                    circle.y + circle.radius * Math.cos(theta)
            ));
        }
        points.add(end);
        return points;
    }

    /**
     * The arc angle in radians
     */
    private double arcs() {
        double arcs = this.turn * (this.angle2() - this.angle1());
        if (arcs < 0) arcs += 2 * Math.PI;
        return arcs;
    }

    /**
     * Angle from center of circle to start
     */
    private double angle1() {
        return Math.atan2(start.x - circle.x, start.y - circle.y);
    }

    /**
     * Angle from center of circle to end
     */
    private double angle2() {
        return Math.atan2(end.x - circle.x, end.y - circle.y);
    }
}
