package ws.baseline.paradrone.geo;

public class Point3V extends PointV {
    public final double alt;
    public final double climb;

    public Point3V(double x, double y, double alt, double vx, double vy, double climb) {
        super(x, y, vx, vy);
        this.alt = alt;
        this.climb = climb;
    }
}
