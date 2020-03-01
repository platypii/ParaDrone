package ws.baseline.autopilot.map;

import ws.baseline.autopilot.DroneState;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

        // Add map layers
        addLayer(new MyPositionLayer());
        addLayer(new LandingLayer());
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
