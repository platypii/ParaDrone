package ws.baseline.autopilot.geo;

public class PointV extends Point {
    public final double vx;
    public final double vy;

    public PointV(double x, double y, double vx, double vy) {
        super(x, y);
        this.vx = vx;
        this.vy = vy;
    }
}
