package ws.baseline.paradrone.geo;

import androidx.annotation.NonNull;
import java.util.Locale;
import timber.log.Timber;

public class Point {
    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        if (Double.isNaN(x) || Double.isNaN(y)) {
            Timber.e("Invalid point %s", this);
        }
    }

    public double distance(@NonNull Point other) {
        final double dx = x - other.x;
        final double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "PointV(%f, %f)", x, y);
    }
}
