package ws.baseline.paradrone.geo;

import androidx.annotation.NonNull;
import java.util.List;

public interface PathLike {

    PointV start();
    PointV end();

    @NonNull
    Path fly(double distance);

    double length();

    @NonNull
    List<Point> render();

}
