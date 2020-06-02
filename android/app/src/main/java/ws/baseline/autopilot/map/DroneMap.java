package ws.baseline.autopilot.map;

import ws.baseline.autopilot.bluetooth.APEvent;
import ws.baseline.autopilot.plan.PlanEvent;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DroneMap extends MapFragment {

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        super.onMapReady(map);

        // Add map layers
        addLayer(new MyPositionLayer());
        addLayer(new LandingLayer());
        addLayer(new PlanLayer());

        // TODO: Zoom to current phone location if available
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.6, -122.34), 14));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdate(@NonNull APEvent event) {
        updateLayers();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlan(@NonNull PlanEvent event) {
        updateLayers();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
