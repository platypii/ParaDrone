package ws.baseline.autopilot.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a map
 */
public class MapFragment extends SupportMapFragment implements OnMapReadyCallback {

    // Null if Google Play services APK is not available
    @Nullable
    private GoogleMap map;

    private final List<MapLayer> layers = new ArrayList<>();

    public LatLng center() {
        if (map != null) {
            return map.getCameraPosition().target;
        } else {
            return null;
        }
    }

    public double direction() {
        if (map != null) {
            return Math.toRadians(map.getCameraPosition().bearing);
        } else {
            return 0;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;

        // Map defaults
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Show current phone location
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
    }

    void addLayer(@NonNull MapLayer layer) {
        layers.add(layer);
        if (map != null) {
            layer.onAdd(map);
        }
    }

    void updateLayers() {
        for (MapLayer layer : layers) {
            layer.update();
        }
    }

}
