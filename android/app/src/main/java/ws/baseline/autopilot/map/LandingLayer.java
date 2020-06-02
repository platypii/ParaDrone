package ws.baseline.autopilot.map;

import ws.baseline.autopilot.R;
import ws.baseline.autopilot.bluetooth.APLandingZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import timber.log.Timber;

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
        Timber.i("WTF update landing zone %s %s", arrow, APLandingZone.lastLz);
        if (arrow != null) {
            final APLandingZone lz = APLandingZone.lastLz;
            if (lz != null) {
                Timber.i("WTF update landing zone %s", lz);
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
