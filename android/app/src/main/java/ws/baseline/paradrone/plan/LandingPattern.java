package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.Paraglider;
import ws.baseline.paradrone.geo.LandingZone;
import ws.baseline.paradrone.geo.Point3V;

public class LandingPattern {
    // Ground length of final approach
    public final double finalDistance = 150; // meters
    private final Paraglider para;
    private final LandingZone lz;

    public LandingPattern(Paraglider para, LandingZone lz) {
        this.para = para;
        this.lz = lz;
    }

    /**
     * Landing pattern: start of final approach
     */
    public Point3V startOfFinal() {
        return new Point3V(
                -finalDistance * lz.dest.vx,
                -finalDistance * lz.dest.vy,
                lz.dest.alt + finalDistance / para.glide,
                lz.dest.vx,
                lz.dest.vy,
                para.climbRate
        );
    }

    /**
     * Landing pattern: start of base leg
     */
    public Point3V startOfBase(int turn) {
        return new Point3V(
                -finalDistance * (lz.dest.vx - turn * lz.dest.vy),
                -finalDistance * (turn * lz.dest.vx + lz.dest.vy),
                lz.dest.alt + 2 * finalDistance / para.glide,
                -lz.dest.vx,
                -lz.dest.vy,
                para.climbRate
        );
    }

    /**
     * Landing pattern: start of downwind leg
     */
    public Point3V startOfDownwind(int turn) {
        return new Point3V(
                finalDistance * turn * lz.dest.vy,
                -finalDistance * turn * lz.dest.vx,
                lz.dest.alt + 3 * finalDistance / para.glide,
                -lz.dest.vx,
                -lz.dest.vy,
                para.climbRate
        );
    }
}
