package ws.baseline.autopilot.geo;

public class Point {
    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Point other) {
        return Math.hypot(x - other.x, y - other.y);
    }
}
