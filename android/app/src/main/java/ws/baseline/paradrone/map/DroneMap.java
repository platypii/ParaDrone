package ws.baseline.paradrone.map;

import ws.baseline.paradrone.Services;
import ws.baseline.paradrone.bluetooth.ApEvent;
import ws.baseline.paradrone.bluetooth.ApLandingZone;
import ws.baseline.paradrone.plan.PlanEvent;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DroneMap extends MapFragment {
    private static final int SNAPBACK_TIME = 3000; // ms

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

        if (Services.location.lastLoc != null) {
            // TODO: Zoom based on alt, distance
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(Services.location.lastLoc.toLatLng(), 14));
        } else if (ApLandingZone.lastLz != null && ApLandingZone.lastLz.lz != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(ApLandingZone.lastLz.lz.destination.toLatLng(), 14));
        } else {
            // TODO: Zoom to current phone location if available
            // Default fallback
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.6, -122.34), 14));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdate(@NonNull ApEvent event) {
        updateLayers();
        // Center on drone, if map hasn't been touched recently
        if (map != null && Services.location.lastLoc != null && !draging && System.currentTimeMillis() - lastDrag > SNAPBACK_TIME) {
            map.moveCamera(CameraUpdateFactory.newLatLng(Services.location.lastLoc.toLatLng()));
        }
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
