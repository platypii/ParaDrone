package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.geo.GeoPoint;
import ws.baseline.paradrone.Paraglider;
import ws.baseline.paradrone.geo.LandingZone;
import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.Point3V;
import ws.baseline.paradrone.geo.PointV;
import ws.baseline.paradrone.geo.SegmentLine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ws.baseline.paradrone.plan.PlannerNaive.naive;
import static ws.baseline.paradrone.plan.PlannerStraight.straight;
import static ws.baseline.paradrone.plan.PlannerWaypoints.viaWaypoints;
import static ws.baseline.paradrone.plan.ShortestDubins.allDubins;

public class Autopilot {
    private static final double no_turns_below = 30; // meters
    private static final double naiveDistance = 600; // meters
    private static final double lookahead = 3; // seconds

    /**
     * Search for a 3D plan
     */
    public static Path search3(@NonNull Paraglider para, @NonNull LandingZone lz) {
        // Run to where the ball is going
        final GeoPoint next = para.predict(lookahead);
        final Point3V loc = lz.toPoint3V(next);
        return search(loc, para, lz);
    }

    /**
     * Apply autopilot rules, and then search over waypoint paths
     */
    @NonNull
    public static Path search(@NonNull Point3V loc, @NonNull Paraglider para, @NonNull LandingZone lz) {
        final LandingPattern pattern = new LandingPattern(para, lz);
        final double effectiveRadius = para.turnRadius * 1.25;

        // How much farther can we fly with available altitude?
        final double turn_distance_remaining = para.flightDistanceRemaining(loc.alt - no_turns_below);
        final double flight_distance_remaining = para.flightDistanceRemaining(loc.alt);

        final PointV sof = pattern.startOfFinal();
        final double distance2 = loc.x * loc.x + loc.y * loc.y; // squared

        if (loc.vx == 0 && loc.vy == 0) {
            // No velocity, just go straight to lz
            return new Path("Default", new SegmentLine(loc, lz.dest));
        }

        // Construct flight paths
        final Path straightPath = straight(loc).fly(Math.min(1, flight_distance_remaining));

        if (loc.alt <= no_turns_below) {
            // No turns under 100ft
            return straightPath;
        } else if (distance2 > naiveDistance * naiveDistance) {
            // Naive when far away
            final Path naivePath = naive(loc, sof, effectiveRadius);
            if (naivePath != null) {
                return naivePath.fly(turn_distance_remaining).fly(flight_distance_remaining);
            } else {
                return straightPath;
            }
        } else {
            final List<Path> paths = new ArrayList<>();
            paths.addAll(viaWaypoints(loc, pattern, effectiveRadius));
            paths.addAll(Arrays.asList(allDubins(loc, lz.dest, effectiveRadius)));
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
    private static Path bestPlan(@NonNull LandingZone lz, @NonNull List<Path> paths) {
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
    private static double planScore(@NonNull LandingZone lz, @Nullable Path plan) {
        if (plan != null) {
            final double distance = plan.end().distance(lz.dest);
            return distance;
        } else {
            return 100000;
        }
    }
}
