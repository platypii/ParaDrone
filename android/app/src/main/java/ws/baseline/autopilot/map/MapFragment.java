package ws.baseline.autopilot.map;

import ws.baseline.autopilot.DroneState;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Implements a map
 */
public class MapFragment extends SupportMapFragment implements OnMapReadyCallback {

    // Null if Google Play services APK is not available
    @Nullable
    private GoogleMap map;

    private final List<MapLayer> layers = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.6, -122.3), 12));

        // Add map layers
        addLayer(new MyPositionLayer());
        addLayer(new LandingLayer());

        // Zoom to current phone location
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
    }

    @Subscribe
    public void onUpdate(@NonNull DroneState state) {
        updateLayers();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
