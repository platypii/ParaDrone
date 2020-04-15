package ws.baseline.autopilot.plan;

import ws.baseline.autopilot.GeoPoint;
import ws.baseline.autopilot.Paramotor;
import ws.baseline.autopilot.geo.LandingZone;
import ws.baseline.autopilot.geo.Path;
import ws.baseline.autopilot.geo.PointV;

import androidx.annotation.Nullable;

import static ws.baseline.autopilot.geo.Turn.TURN_LEFT;
import static ws.baseline.autopilot.geo.Turn.TURN_RIGHT;
import static ws.baseline.autopilot.plan.PlannerDubins.dubins;
import static ws.baseline.autopilot.plan.PlannerStraight.straight;

public class Search {

    public static Path search(GeoPoint point, LandingZone lz) {
        // How much farther can we fly with available altitude?
        final double alt = point.alt - lz.destination.alt;
        final double distance = Paramotor.flightDistanceRemaining(alt);
        final double r = Paramotor.turnRadius;
        final PointV loc = lz.toPointV(point);

        // Construct flight paths
        final Path straightPath = straight(loc).fly(distance);
        final Path[] paths = {
            dubins(loc, lz.dest, r, TURN_RIGHT, TURN_RIGHT), // rsr
            dubins(loc, lz.dest, r, TURN_RIGHT, TURN_LEFT), // rsl
            dubins(loc, lz.dest, r, TURN_LEFT, TURN_RIGHT), // lsr
            dubins(loc, lz.dest, r, TURN_LEFT, TURN_LEFT), // lsl
            // naive(params),
            straightPath
        };
        Path best = bestPlan(lz, paths);
        if (best == null) {
            best = straightPath;
        }
        return best;
    }

    /**
     * Find the path that minimizes landing error
     */
    @Nullable
    private static Path bestPlan(LandingZone lz, Path[] paths) {
        Path best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (Path path : paths) {
            final double dist = path.end().distance(lz.dest);
            if (dist < bestScore) {
                best = path;
                bestScore = dist;
            }
        }
        return best;
    }
}
