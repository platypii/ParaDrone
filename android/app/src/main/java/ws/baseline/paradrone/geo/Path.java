package ws.baseline.paradrone.geo;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import timber.log.Timber;

public class Path implements PathLike {
    public final String name;
    public final List<Segment> segments;

    public Path(String name, Segment... segments) {
        this(name, Arrays.asList(segments));
    }
    public Path(String name, List<Segment> segments) {
        this.name = name;
        this.segments = segments;
        if (this.segments.isEmpty()) {
            Timber.e("Invalid empty path");
        }
    }

    @Override
    public PointV start() {
        return segments.get(0).start();
    }

    @Override
    public PointV end() {
        if (segments.isEmpty()) {
            Timber.e("Empty path %s", name);
        }
        return segments.get(segments.size() - 1).end();
    }

    @NonNull
    @Override
    public Path fly(double distance) {
        final List<Segment> trimmed = new ArrayList<>();
        double flown = 0;
        int i = 0;
        for (; i < segments.size() - 1; i++) {
            final Segment segment = segments.get(i);
            final double segmentLength = segment.length();
            if (distance < flown + segmentLength) {
                // End point is within segment
                break;
            } else {
                trimmed.add(segment);
                flown += segmentLength;
            }
        }
        // Fly last segment
        trimmed.addAll(segments.get(i).fly(distance - flown).segments);
        return new Path(name, trimmed);
    }

    @Override
    public double length() {
        double len = 0;
        for (Segment segment : segments) {
            len += segment.length();
        }
        return len;
    }

    @NonNull
    @Override
    public List<Point> render() {
        final List<Point> points = new ArrayList<>();
        for (Segment segment : segments) {
            points.addAll(segment.render());
        }
        return points;
    }
}
