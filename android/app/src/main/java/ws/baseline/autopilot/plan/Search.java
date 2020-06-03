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
import static ws.baseline.autopilot.plan.PlannerNaive.naive;
import static ws.baseline.autopilot.plan.PlannerStraight.straight;

public class Search {
    private static final double no_turns_below = 30; // meters

    public static Path search(GeoPoint point, LandingZone lz) {
        final PointV loc = lz.toPointV(point);

        // How much farther can we fly with available altitude?
        final double alt_agl = point.alt - lz.destination.alt;
        final double turn_distance_remaining = Paramotor.flightDistanceRemaining(alt_agl - no_turns_below);
        final double flight_distance_remaining = Paramotor.flightDistanceRemaining(alt_agl);

        final PointV sof = lz.startOfFinal();
        final double r = Paramotor.turnRadius;
        final double distance = Math.hypot(loc.x, loc.y);

        // Construct flight paths
        final Path straightPath = straight(loc).fly(flight_distance_remaining);
        final Path naivePath = naive(loc, sof, r);

        if (alt_agl < no_turns_below) {
            // No turns under 100ft
            return straightPath;
        } else if (distance > 1000 && naivePath != null) {
            return naivePath.fly(turn_distance_remaining).fly(flight_distance_remaining);
        } else {
            final Path[] paths = {
                    dubins(loc, sof, r, TURN_RIGHT, TURN_RIGHT), // rsr
                    dubins(loc, sof, r, TURN_RIGHT, TURN_LEFT), // rsl
                    dubins(loc, sof, r, TURN_LEFT, TURN_RIGHT), // lsr
                    dubins(loc, sof, r, TURN_LEFT, TURN_LEFT), // lsl
                    // naivePath,
                    straightPath
            };
            for (int i = 0; i < paths.length; i++) {
                if (paths[i] != null) {
                    paths[i] = paths[i].fly(turn_distance_remaining).fly(flight_distance_remaining);
                }
            }
            Path best = bestPlan(lz, paths);
            if (best == null) {
                best = straightPath;
            }
            return best;
        }
    }

    /**
     * Find the path that minimizes landing error
     */
    @Nullable
    private static Path bestPlan(LandingZone lz, Path[] paths) {
        Path best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (Path path : paths) {
            if (path != null) {
                final double score = planScore(lz, path);
                if (score < bestScore) {
                    best = path;
                    bestScore = score;
                }
            }
        }
        return best;
    }

    /**
     * Plan score. Lower is better.
     */
    private static double planScore(LandingZone lz, Path plan) {
        if (plan != null) {
            final double distance = plan.end().distance(lz.dest);
            return distance;
        } else {
            return 100000;
        }
    }
}
