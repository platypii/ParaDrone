package ws.baseline.paradrone.map;

import ws.baseline.paradrone.R;
import ws.baseline.paradrone.bluetooth.ApLandingZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;

public class LandingLayer extends MapLayer {

    @Nullable
    private GroundOverlay arrow;

    @Override
    public void onAdd(@NonNull GoogleMap map) {
        arrow = map.addGroundOverlay(new GroundOverlayOptions()
                .position(new LatLng(0, 0), 20)
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
                .anchor(0.5f, 0)
                .visible(false)
        );
    }

    @Override
    public void update() {
        if (arrow != null) {
            final ApLandingZone lz = ApLandingZone.lastLz;
            if (lz != null && lz.lz != null) {
                arrow.setPosition(lz.lz.destination.toLatLng());
                arrow.setDimensions((float) lz.lz.finalDistance * 0.2f);
                arrow.setBearing((float) Math.toDegrees(lz.lz.landingDirection));
                if (lz.pending) {
                    arrow.setTransparency(0.25f);
                } else {
                    arrow.setTransparency(0);
                }
                arrow.setVisible(true);
            } else {
                arrow.setVisible(false);
            }
        }
    }
}
