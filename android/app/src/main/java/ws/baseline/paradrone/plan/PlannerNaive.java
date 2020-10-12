package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.geo.Circle;
import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.Point;
import ws.baseline.paradrone.geo.PointV;
import ws.baseline.paradrone.geo.SegmentLine;
import ws.baseline.paradrone.geo.SegmentTurn;
import ws.baseline.paradrone.util.Numbers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

import static ws.baseline.paradrone.geo.Turn.TURN_LEFT;
import static ws.baseline.paradrone.geo.Turn.TURN_RIGHT;

class PlannerNaive {

    /**
     * Fly naively to a waypoint.
     * This path will consist of a turn, plus a straight line to the target.
     * You will probably not arrive at your destination in the DIRECTION you want though.
     */
    @Nullable
    static Path naive(@NonNull PointV loc, @NonNull PointV dest, double turnRadius) {
        final double velocity = Math.sqrt(loc.vx * loc.vx + loc.vy * loc.vy);
        if (velocity == 0) {
            Timber.i("Zero velocity no tangent");
            return null;
        }
        final double delta_x = dest.x - loc.x;
        final double delta_y = dest.y - loc.y;
        final double delta = Math.sqrt(delta_x * delta_x + delta_y * delta_y);
        if (delta < 2 * turnRadius) {
            // Timber.w("Naive planner on top of lz");
            return null;
        }
        // Is dest on our left or right?
        final double dot = delta_y * loc.vx - delta_x * loc.vy;
        final int turn1 = dot > 0 ? TURN_LEFT : TURN_RIGHT;
        // Compute path for naive
        final Circle c1 = new Circle(
                loc.x + turn1 * turnRadius * loc.vy / velocity,
                loc.y - turn1 * turnRadius * loc.vx / velocity,
                turnRadius
        );
        // Angle from circle center to target
        final double center_angle = Math.atan2(dest.x - c1.x, dest.y - c1.y);
        // Commute
        final double cdest = Numbers.hypot(c1.x - dest.x, c1.y - dest.y);
        final double offset = turn1 * Math.asin(turnRadius / cdest);
        final double commute_angle = center_angle + offset;
        // const commute_length = Math.sqrt(cdest * cdest - r * r);
        // Last touch of first dubin circle (start of commute home)
        final Point comm1 = new Point(
                c1.x - turn1 * turnRadius * Math.cos(commute_angle),
                c1.y + turn1 * turnRadius * Math.sin(commute_angle)
        );
        return new Path(
                "naive",
                new SegmentTurn(c1, loc, comm1, turn1),
                new SegmentLine(comm1, dest)
        );
    }
}
