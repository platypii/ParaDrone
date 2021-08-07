package ws.baseline.paradrone.map;

import ws.baseline.paradrone.geo.GeoPoint;
import ws.baseline.paradrone.R;
import ws.baseline.paradrone.Services;
import ws.baseline.paradrone.util.Numbers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MyPositionLayer extends MapLayer {

    @Nullable
    private Marker myPositionMarker;
    @NonNull
    private final BitmapDescriptor myposition1 = BitmapDescriptorFactory.fromResource(R.drawable.myposition1);
    @NonNull
    private final BitmapDescriptor myposition2 = BitmapDescriptorFactory.fromResource(R.drawable.myposition2);

    @Override
    public void onAdd(@NonNull GoogleMap map) {
        myPositionMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .visible(false)
                .icon(myposition2)
                .anchor(0.5f, 0.5f)
                .flat(true)
        );
    }

    @Override
    public void update() {
        final GeoPoint ll = Services.location.lastLoc;
        if (myPositionMarker != null && ll != null) {
            myPositionMarker.setVisible(true);
            myPositionMarker.setPosition(ll.toLatLng());
            final double groundSpeed = ll.groundSpeed();
            final double bearing = ll.bearing();
            if (Numbers.isReal(bearing) && groundSpeed > 0.1) {
                // Speed > 0.2mph
                myPositionMarker.setIcon(myposition1);
                myPositionMarker.setRotation((float) bearing);
            } else {
                myPositionMarker.setIcon(myposition2);
            }
        }
    }

}
