package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.Point3V;
import ws.baseline.paradrone.geo.Segment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ws.baseline.paradrone.geo.Turn.TURN_LEFT;
import static ws.baseline.paradrone.geo.Turn.TURN_RIGHT;
import static ws.baseline.paradrone.plan.ShortestDubins.shortestDubins;

class PlannerWaypoints {

    /**
     * Find the nest path to fly between a set of waypoints.
     * Waypoints include a velocity component to indicate the direction to arrive.
     * In between each waypoint, follow the shortest dubins path.
     */
    @NonNull
    static List<Path> viaWaypoints(@NonNull Point3V loc, @NonNull LandingPattern pattern, double turnRadius) {
        // Fly straight for 10s
        final Point3V straight = new Point3V(
                loc.x + 10 * loc.vx,
                loc.y + 10 * loc.vy,
                loc.alt + 10 * loc.climb,
                loc.vx,
                loc.vy,
                loc.climb
        );
        final List<Point3V> leftPattern = Arrays.asList(straight, pattern.startOfDownwind(TURN_LEFT), pattern.startOfBase(TURN_LEFT), pattern.startOfFinal());
        final List<Point3V> rightPattern = Arrays.asList(straight, pattern.startOfDownwind(TURN_RIGHT), pattern.startOfBase(TURN_RIGHT), pattern.startOfFinal());

        final List<Path> plans = new ArrayList<>();
        plans.addAll(searchPattern(loc, leftPattern, turnRadius));
        plans.addAll(searchPattern(loc, rightPattern, turnRadius));
        return plans;
    }

    /**
     * Search all suffixes of a flight pattern.
     * In between each waypoint, follow the shorest dubins path.
     */
    @NonNull
    private static List<Path> searchPattern(@NonNull Point3V loc, @NonNull List<Point3V> pattern, double r) {
        // Pre-compute shortest dubins paths from pattern[i] to pattern[i+1]
        final List<Path> steps = new ArrayList<>();
        for (int i = 0; i < pattern.size() - 1; i++) {
            steps.add(shortestDubins(pattern.get(i), pattern.get(i + 1), r));
        }
        // Construct path for all suffixes
        final List<Path> paths = new ArrayList<>();
        for (int i = 0; i < pattern.size(); i++) {
            // Construct path for [loc, pattern[i], ..., pattern[n]]
            final Path first = shortestDubins(loc, pattern.get(i), r);
            final Path path = catPaths(first, steps.subList(i, steps.size()));
            if (path != null) paths.add(path);
        }
        return paths;
    }

    /**
     * Concatenate paths.
     * If any of the paths are null, return null.
     * TODO: Check for empty segments?
     */
    @Nullable
    private static Path catPaths(@Nullable Path first, @NonNull List<Path> paths) {
        if (first == null) return null; // No shortest path
        final List<Segment> segments = new ArrayList<>(first.segments);
        for (Path path : paths) {
            if (path == null) return null; // No shortest path
            segments.addAll(path.segments);
        }
        return new Path("waypoints", segments);
    }
}
