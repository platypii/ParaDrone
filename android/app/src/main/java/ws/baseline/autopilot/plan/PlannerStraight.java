package ws.baseline.autopilot.plan;

import ws.baseline.autopilot.geo.Path;
import ws.baseline.autopilot.geo.Point;
import ws.baseline.autopilot.geo.PointV;
import ws.baseline.autopilot.geo.SegmentLine;

import androidx.annotation.NonNull;

class PlannerStraight {

    /**
     * Fly straight forever.
     */
    @NonNull
    static Path straight(PointV loc) {
        final Point target = new Point(loc.x + loc.vx, loc.y + loc.vy);
        return new Path(
                new SegmentLine(loc, target)
        );
    }
}
