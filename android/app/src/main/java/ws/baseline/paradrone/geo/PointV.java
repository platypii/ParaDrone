package ws.baseline.paradrone.geo;

import androidx.annotation.NonNull;
import java.util.Locale;
import timber.log.Timber;

public class PointV extends Point {
    public final double vx; // m/s
    public final double vy; // m/s

    public PointV(double x, double y, double vx, double vy) {
        super(x, y);
        this.vx = vx;
        this.vy = vy;
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(vx) || Double.isNaN(vy)) {
            Timber.e("Invalid point %s", this);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "PointV(%f, %f, %f, %f)", x, y, vx, vy);
    }
}
