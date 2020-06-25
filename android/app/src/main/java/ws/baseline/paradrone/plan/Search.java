package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.Paramotor;
import ws.baseline.paradrone.geo.LandingZone;
import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.Point3V;
import ws.baseline.paradrone.geo.PointV;
import ws.baseline.paradrone.geo.SegmentLine;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static ws.baseline.paradrone.plan.PlannerNaive.naive;
import static ws.baseline.paradrone.plan.PlannerStraight.straight;
import static ws.baseline.paradrone.plan.PlannerWaypoints.viaWaypoints;

public class Search {
    private static final double no_turns_below = 30; // meters

    public static Path search(Point3V loc, LandingZone lz) {
        // How much farther can we fly with available altitude?
        final double alt_agl = loc.alt - lz.destination.alt;
        final double turn_distance_remaining = Paramotor.flightDistanceRemaining(alt_agl - no_turns_below);
        final double flight_distance_remaining = Paramotor.flightDistanceRemaining(alt_agl);

        final PointV sof = lz.startOfFinal();
        final double r = Paramotor.turnRadius;
        final double distance = Math.hypot(loc.x, loc.y);

        if (loc.vx == 0 && loc.vy == 0) {
            // No velocity, just go straight to lz
            return new Path("default", new SegmentLine(loc, lz.dest));
        }

        // Construct flight paths
        final Path straightPath = straight(loc).fly(Math.min(1, flight_distance_remaining));
        final Path naivePath = naive(loc, sof, r);

        if (alt_agl <= no_turns_below) {
            // No turns under 100ft
            return straightPath;
        } else if (distance > 1000 && naivePath != null) {
            return naivePath.fly(turn_distance_remaining).fly(flight_distance_remaining);
        } else {
            final List<Path> paths = new ArrayList<>();
            paths.addAll(viaWaypoints(loc, lz));
//            paths.add(dubins(loc, sof, r, TURN_RIGHT, TURN_RIGHT)); // rsr
//            paths.add(dubins(loc, sof, r, TURN_RIGHT, TURN_LEFT)); // rsl
//            paths.add(dubins(loc, sof, r, TURN_LEFT, TURN_RIGHT)); // lsr
//            paths.add(dubins(loc, sof, r, TURN_LEFT, TURN_LEFT)); // lsl
//            paths.add(naivePath);
            paths.add(straightPath);
            for (int i = 0; i < paths.size(); i++) {
                if (paths.get(i) != null) {
                    // Trim to turnable altitude
                    paths.set(i, paths.get(i).fly(turn_distance_remaining).fly(flight_distance_remaining));
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
    private static Path bestPlan(LandingZone lz, List<Path> paths) {
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
