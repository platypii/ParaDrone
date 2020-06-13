package ws.baseline.autopilot.plan;

import ws.baseline.autopilot.Paramotor;
import ws.baseline.autopilot.geo.LandingZone;
import ws.baseline.autopilot.geo.Path;
import ws.baseline.autopilot.geo.Point3V;
import ws.baseline.autopilot.geo.Segment;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ws.baseline.autopilot.geo.Turn.TURN_LEFT;
import static ws.baseline.autopilot.geo.Turn.TURN_RIGHT;
import static ws.baseline.autopilot.plan.ShortestDubins.shortestDubins;

class PlannerWaypoints {

    /**
     * Find the nest path to fly between a set of waypoints.
     * Waypoints include a velocity component to indicate the direction to arrive.
     */
    @NonNull
    static List<Path> viaWaypoints(Point3V loc, LandingZone lz) {
        final List<Point3V> rightDownwind = Arrays.asList(loc, lz.startOfDownwind(TURN_RIGHT), lz.startOfBase(TURN_RIGHT), lz.startOfFinal());
        final List<Point3V> leftDownwind = Arrays.asList(loc, lz.startOfDownwind(TURN_LEFT), lz.startOfBase(TURN_LEFT), lz.startOfFinal());
        final List<Point3V> finalDirect = Arrays.asList(loc, lz.startOfFinal());
        final List<Point3V> lzDirect = Arrays.asList(loc, lz.dest);
        final List<List<Point3V>> patterns = Arrays.asList(rightDownwind, leftDownwind, finalDirect, lzDirect);

        final List<Path> plans = new ArrayList<>();
        for (List<Point3V> pattern : patterns) {
            final Path plan = waypointPath(pattern);
            if (plan != null) {
                plans.add(plan);
            }
        }
        return plans;
    }

    private static Path waypointPath(List<Point3V> waypoints) {
        final List<Segment> segments = new ArrayList<>();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Path shortest = shortestDubins(waypoints.get(i), waypoints.get(i + 1), Paramotor.turnRadius);
            if (shortest == null) {
                // No shortest path
                return null;
            }
            segments.addAll(shortest.segments);
        }
        return new Path("waypoints", segments);
    }

}
