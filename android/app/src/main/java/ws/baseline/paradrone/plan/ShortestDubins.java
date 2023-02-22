package ws.baseline.paradrone.plan;

import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.PointV;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static ws.baseline.paradrone.geo.Turn.TURN_LEFT;
import static ws.baseline.paradrone.geo.Turn.TURN_RIGHT;
import static ws.baseline.paradrone.plan.PlannerDubins.dubins;

public class ShortestDubins {

    @NonNull
    static Path[] allDubins(@NonNull PointV loc, @NonNull PointV dest, double r) {
        return new Path[]{
                dubins(loc, dest, r, TURN_RIGHT, TURN_RIGHT),
                dubins(loc, dest, r, TURN_RIGHT, TURN_LEFT),
                dubins(loc, dest, r, TURN_LEFT, TURN_RIGHT),
                dubins(loc, dest, r, TURN_LEFT, TURN_LEFT)
        };
    }

    @Nullable
    static Path shortestDubins(@NonNull PointV loc, @NonNull PointV dest, double r) {
        return shortestPath(allDubins(loc, dest, r));
    }

    @Nullable
    private static Path shortestPath(@NonNull Path[] paths) {
        Path best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (Path path : paths) {
            if (path != null) {
                final double score = path.length();
                if (score < bestScore) {
                    best = path;
                    bestScore = score;
                }
            }
        }
        return best;
    }
}
