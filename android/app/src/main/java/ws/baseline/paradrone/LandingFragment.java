package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.ApLandingZone;
import ws.baseline.paradrone.databinding.LandingFragmentBinding;
import ws.baseline.paradrone.geo.LandingZone;
import ws.baseline.paradrone.map.DroneMap;
import ws.baseline.paradrone.map.Elevation;
import ws.baseline.paradrone.util.Convert;
import ws.baseline.paradrone.util.Numbers;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.model.LatLng;
import timber.log.Timber;

public class LandingFragment extends Fragment {

    private LandingFragmentBinding binding;
    private Handler handler = new Handler();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LandingFragmentBinding.inflate(inflater, container, false);

        binding.getElevation.setOnClickListener((e) -> {
            final LatLng ll = getMapCenter();
            final Context ctx = getContext();
            if (ll != null && ctx != null) {
                Timber.i("Getting elevation %s", ll);
                Elevation.get(ctx, ll, (elevation) -> {
                    Timber.i("Got elevation %f", elevation);
                    handler.post(() -> binding.lzElevation.setText(Convert.distance(elevation)));
                });
            } else {
                Timber.e("Failed to get map center");
            }
        });
        binding.setLandingZone.setOnClickListener((e) -> {
            final LandingZone lz = getLandingZone();
            Timber.i("Setting landing zone %s", lz);
            ApLandingZone.setPending(lz);
            Services.bluetooth.actions.setLandingZone(lz);
            // TODO: Wait for confirmation
            ViewState.setMode(ViewState.ViewMode.HOME);
        });
        binding.lzCancel.setOnClickListener((e) -> {
            getParentFragmentManager().popBackStack();
        });

        return binding.getRoot();
    }

    @Nullable
    private LatLng getMapCenter() {
        final DroneMap frag = (DroneMap) getParentFragmentManager().findFragmentById(R.id.map);
        if (frag != null) {
            return frag.center();
        } else {
            Timber.e("Failed to find map fragment");
            return null;
        }
    }

    @Nullable
    private LandingZone getLandingZone() {
        final DroneMap frag = (DroneMap) getParentFragmentManager().findFragmentById(R.id.map);
        if (frag != null) {
            final LatLng ll = frag.center();
            final double landingDirection = frag.direction(); // radians
            final double elevation = Numbers.parseDistance(binding.lzElevation.getText().toString());
            if (ll != null && !Double.isNaN(elevation)) {
                return new LandingZone(ll.latitude, ll.longitude, elevation, landingDirection);
            } else {
                return null;
            }
        } else {
            Timber.e("Failed to find map fragment");
            return null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewState.setMode(ViewState.ViewMode.LZ);
    }
}
