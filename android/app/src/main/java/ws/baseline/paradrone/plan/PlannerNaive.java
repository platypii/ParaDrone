package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.geo.Circle;
import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.Point;
import ws.baseline.paradrone.geo.PointV;
import ws.baseline.paradrone.geo.SegmentLine;
import ws.baseline.paradrone.geo.SegmentTurn;

import androidx.annotation.Nullable;

import static ws.baseline.paradrone.geo.Turn.TURN_LEFT;
import static ws.baseline.paradrone.geo.Turn.TURN_RIGHT;

class PlannerNaive {

    /**
     * Fly naively to a waypoint.
     * This path will consist of a turn, plus a straight line to the target.
     * You will probably not arrive at your destination in the DIRECTION you want though.
     */
    @Nullable
    static Path naive(PointV loc, PointV dest, double turnRadius) {
        if (Math.hypot(loc.x - dest.x, loc.y - dest.y) < 2 * turnRadius) {
            // Log.w(TAG, "Naive planner on top of lz");
            return null;
        }
        // bearing from loc to destination
        final double bearing = Math.atan2(dest.x - loc.x, dest.y - loc.y);
        // velocity bearing
        final double yaw = Math.atan2(loc.vx, loc.vy);
        // shorter to turn right or left?
        final double delta = bearing - yaw;
        final int turn1 = delta < 0 ? TURN_LEFT : TURN_RIGHT;
        // Compute path for naive
        final double velocity = Math.hypot(loc.vx, loc.vy);
        final Circle c1 = new Circle(
                loc.x + turn1 * turnRadius * loc.vy / velocity,
                loc.y - turn1 * turnRadius * loc.vx / velocity,
                turnRadius
        );
        // Angle from circle center to target
        final double center_angle = Math.atan2(dest.x - c1.x, dest.y - c1.y);
        // Commute
        final double cdest = Math.hypot(c1.x - dest.x, c1.y - dest.y);
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
