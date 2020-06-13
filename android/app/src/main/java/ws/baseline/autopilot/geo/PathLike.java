package ws.baseline.autopilot.geo;

import java.util.List;

public interface PathLike {

    PointV start();
    PointV end();

    Path fly(double distance);

    double length();

    List<Point> render();

}
