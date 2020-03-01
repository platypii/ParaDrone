package ws.baseline.autopilot.map;

import ws.baseline.autopilot.DroneState;
import ws.baseline.autopilot.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import java.util.ArrayList;
import java.util.List;

public class LandingLayer extends MapLayer {

    @Nullable
    private Marker landingMarker;
    @Nullable
    private Polyline landingPath;
    private final List<LatLng> landingPoints = new ArrayList<>();

    @Override
    public void onAdd(@NonNull GoogleMap map) {
        // Add projected landing zone
        landingMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .visible(false)
                .title("landing")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_target))
                .anchor(0.5f, 0.5f)
        );
        // Add line to projected landing zone
        landingPath = map.addPolyline(new PolylineOptions()
                .visible(false)
                .width(10)
                .color(0x66ff0000)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
        );
    }

    @Override
    public void update() {
        if (landingMarker != null && landingPath != null) {
            final DroneState state = DroneState.get();
            if (state.lz != null) {
                landingMarker.setPosition(state.lz.destination.toLatLng());
                landingMarker.setVisible(true);
                landingPoints.clear();
                landingPoints.add(state.currentLocation.toLatLng());
                landingPoints.add(state.lz.destination.toLatLng());
                landingPath.setPoints(landingPoints);
                landingPath.setVisible(true);
            } else {
                landingMarker.setVisible(false);
                landingPath.setVisible(false);
            }
        }
    }
}
