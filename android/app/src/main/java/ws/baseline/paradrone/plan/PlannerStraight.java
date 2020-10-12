package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.Point;
import ws.baseline.paradrone.geo.PointV;
import ws.baseline.paradrone.geo.SegmentLine;

import androidx.annotation.NonNull;

class PlannerStraight {

    /**
     * Fly straight forever.
     */
    @NonNull
    static Path straight(@NonNull PointV loc) {
        final Point target = new Point(loc.x + loc.vx, loc.y + loc.vy);
        return new Path(
                "straight",
                new SegmentLine(loc, target)
        );
    }
}
