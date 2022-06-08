package ws.baseline.paradrone.map;

import android.Manifest;
import android.app.Activity;
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
 * Implements a generic map
 */
public class MapFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {
    // Drag listener
    boolean draging = false;
    long lastDrag = 0;

    // Null if Google Play services APK is not available
    @Nullable
    public GoogleMap map;

    private final List<MapLayer> layers = new ArrayList<>();

    @Nullable
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

        // Drag listener
        map.setOnCameraMoveStartedListener(this);
        map.setOnCameraIdleListener(this);

        // Show current phone location
        final Activity activity = getActivity();
        if (activity != null && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == REASON_GESTURE) {
            draging = true;
        }
    }

    @Override
    public void onCameraIdle() {
        if (draging) {
            lastDrag = System.currentTimeMillis();
            draging = false;
        }
    }
}
