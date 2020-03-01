package ws.baseline.autopilot.map;

import ws.baseline.autopilot.DroneState;
import ws.baseline.autopilot.util.Numbers;
import ws.baseline.autopilot.R;

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
        final DroneState state = DroneState.get();
        if (myPositionMarker != null && state.currentLocation != null) {
            myPositionMarker.setVisible(true);
            myPositionMarker.setPosition(state.currentLocation.toLatLng());
            final double groundSpeed = state.currentLocation.groundSpeed();
            final double bearing = state.currentLocation.bearing();
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
