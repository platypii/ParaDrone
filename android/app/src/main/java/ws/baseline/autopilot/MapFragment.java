package ws.baseline.autopilot;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Implements a map
 */
public class MapFragment extends SupportMapFragment implements OnMapReadyCallback {

    // Null if Google Play services APK is not available
    @Nullable
    private GoogleMap map;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

}
