package ws.baseline.autopilot.map;

import ws.baseline.autopilot.R;
import ws.baseline.autopilot.Services;
import ws.baseline.autopilot.geo.LandingZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LandingLayer extends MapLayer {

    @Nullable
    private Marker landingMarker;

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
    }

    @Override
    public void update() {
        if (landingMarker != null) {
            final LandingZone lz = Services.flightComputer.lz;
            if (lz != null) {
                landingMarker.setPosition(lz.destination.toLatLng());
                landingMarker.setVisible(true);
                if (Services.flightComputer.lzPending) {
                    landingMarker.setAlpha(0.5f);
                } else {
                    landingMarker.setAlpha(1);
                }
            } else {
                landingMarker.setVisible(false);
            }
        }
    }
}
