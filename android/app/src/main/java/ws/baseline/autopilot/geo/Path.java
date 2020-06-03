package ws.baseline.autopilot.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Path implements PathLike {
    private final List<Segment> segments;

    public Path(Segment... segments) {
        this.segments = Arrays.asList(segments);
    }
    private Path(List<Segment> segments) {
        this.segments = segments;
    }

    @Override
    public PointV start() {
        return segments.get(0).start();
    }

    @Override
    public PointV end() {
        return segments.get(segments.size() - 1).end();
    }

    @Override
    public Path fly(double distance) {
        final List<Segment> trimmed = new ArrayList<>();
        double flown = 0;
        int i = 0;
        for (; i < segments.size(); i++) {
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
        return new Path(trimmed);
    }

    @Override
    public double length() {
        double len = 0;
        for (Segment segment : segments) {
            len += segment.length();
        }
        return len;
    }

    @Override
    public List<Point> render() {
        final List<Point> points = new ArrayList<>();
        for (Segment segment : segments) {
            points.addAll(segment.render());
        }
        return points;
    }
}
