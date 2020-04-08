package ws.baseline.autopilot.geo;

import java.util.List;

public interface PathLike {

    Point start();
    Point end();

    PathLike fly(double distance);

    double length();

    List<Point> render();

}
