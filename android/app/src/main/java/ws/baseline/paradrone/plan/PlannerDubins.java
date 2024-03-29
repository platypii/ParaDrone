package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.geo.Circle;
import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.Point;
import ws.baseline.paradrone.geo.PointV;
import ws.baseline.paradrone.geo.SegmentLine;
import ws.baseline.paradrone.geo.SegmentTurn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

import static ws.baseline.paradrone.geo.Turn.TURN_LEFT;

class PlannerDubins {

    /**
     * Find dubins path.
     */
    @Nullable
    static Path dubins(@NonNull PointV loc, @NonNull PointV dest, double turnRadius, int turn1, int turn2) {
        // First dubins circle, perpendicular to velocity
        final double velocity = Math.sqrt(loc.vx * loc.vx + loc.vy * loc.vy);
        if (velocity == 0) {
            Timber.w("Zero velocity no tangent");
            return null;
        }
        final Circle c1 = new Circle(
                loc.x + turn1 * turnRadius * loc.vy / velocity,
                loc.y - turn1 * turnRadius * loc.vx / velocity,
                turnRadius
        );
        // Second dubins circle
        final double dest_velocity = Math.sqrt(dest.vx * dest.vx + dest.vy * dest.vy);
        if (dest_velocity == 0) {
            Timber.w("Zero velocity no tangent");
            return null;
        }
        final Circle c2 = new Circle(
                dest.x + turn2 * turnRadius * dest.vy / dest_velocity,
                dest.y - turn2 * turnRadius * dest.vx / dest_velocity,
                turnRadius
        );
        // Delta of dubin circles
        final double cx_delta = c2.x - c1.x;
        final double cy_delta = c2.y - c1.y;
        final double c_dist = Math.sqrt(cx_delta * cx_delta + cy_delta * cy_delta);
        if (turn1 != turn2 && c_dist < 2 * turnRadius) {
            // println("Intersecting dubins circles", c2, dest)
            return null;
        }
        // Angle from center to center
        final double center_angle = Math.atan2(cx_delta, cy_delta);
        // Commute
        // If turn1 != turn2, then cross circles
        double turn_delta = 0;
        if (turn1 != turn2) {
            turn_delta = (turn1 - turn2) * turnRadius / c_dist;
            turn_delta = Math.max(-1, Math.min(1, turn_delta));
            turn_delta = Math.asin(turn_delta);
        }
        final double commute_angle = center_angle + turn_delta;
        // const commute_length = Math.sqrt(c_dist * c_dist - turn_delta * turn_delta)
        if (Double.isNaN(commute_angle)) {
            // Happens when c1 intersects c2
            Timber.e("NaN commute angle");
        }
        // Last touch of first dubin circle (start of commute home)
        final Point comm1 = new Point(
                c1.x - turn1 * turnRadius * Math.cos(commute_angle),
                c1.y + turn1 * turnRadius * Math.sin(commute_angle)
        );
        // First touch of second dubin circle (beginning of turn to final)
        final Point comm2 = new Point(
                c2.x - turn2 * turnRadius * Math.cos(commute_angle),
                c2.y + turn2 * turnRadius * Math.sin(commute_angle)
        );
        final String name = turn1 == TURN_LEFT ? "DubinL" : "DubinR";
        return new Path(
                name,
                new SegmentTurn(c1, loc, comm1, turn1),
                new SegmentLine(comm1, comm2),
                new SegmentTurn(c2, comm2, dest, turn2)
        );
    }

}
