package ws.baseline.paradrone.map;

import ws.baseline.paradrone.geo.LandingZone;
import ws.baseline.paradrone.geo.Path;
import ws.baseline.paradrone.geo.Point;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import java.util.ArrayList;
import java.util.List;

public class PathLayer extends MapLayer {

    @Nullable
    private Polyline polyline;
    private final List<LatLng> coords = new ArrayList<>();

    private Path path;
    private LandingZone lz;

    void setPath(Path path, LandingZone lz) {
        this.path = path;
        this.lz = lz;
    }

    @Override
    public void onAdd(@NonNull GoogleMap map) {
        polyline = map.addPolyline(new PolylineOptions()
                .visible(false)
                .width(10)
                .color(0x66ff0000)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
        );
    }

    @Override
    public void update() {
        if (polyline != null && path != null) {
            coords.clear();
            for (Point point : path.render()) {
                coords.add(lz.toLatLng(point).toLatLng());
            }
            polyline.setPoints(coords);
            polyline.setVisible(true);
        } else if (polyline != null) {
            polyline.setVisible(false);
        }
    }
}
