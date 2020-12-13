package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.ApLandingZone;
import ws.baseline.paradrone.databinding.LandingFragmentBinding;
import ws.baseline.paradrone.geo.LandingZone;
import ws.baseline.paradrone.map.DroneMap;
import ws.baseline.paradrone.map.Elevation;
import ws.baseline.paradrone.util.Convert;
import ws.baseline.paradrone.util.Numbers;

import android.annotation.SuppressLint;
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
import com.google.maps.PendingResult;
import com.google.maps.model.ElevationResult;
import timber.log.Timber;

@SuppressLint("SetTextI18n")
public class LandingFragment extends Fragment {

    private LandingFragmentBinding binding;
    private final Handler handler = new Handler();
    private boolean savePending = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LandingFragmentBinding.inflate(inflater, container, false);

        binding.getElevation.setOnClickListener(this::fetchElevation);
        binding.setLandingZone.setOnClickListener((e) -> {
            if (binding.lzElevation.getText().toString().isEmpty()) {
                savePending = true;
                fetchElevation(null);
            } else {
                save();
            }
        });
        binding.lzCancel.setOnClickListener((e) -> getParentFragmentManager().popBackStack());

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

    private void fetchElevation(View view) {
        final LatLng ll = getMapCenter();
        final Context ctx = getContext();
        if (ll != null && ctx != null) {
            Timber.i("Getting elevation %.6f, %.6f", ll.latitude, ll.longitude);
            binding.landingStatus.setText(R.string.fetching_elevation);
            Elevation.get(ctx, ll, new PendingResult.Callback<ElevationResult>() {
                @Override
                public void onResult(ElevationResult result) {
                    Timber.i("Got elevation %f", result.elevation);
                    handler.post(() -> {
                        binding.landingStatus.setText("");
                        binding.lzElevation.setText(Convert.distance(result.elevation, 0, false));
                        if (savePending) {
                            savePending = false;
                            save();
                        }
                    });
                }
                @Override
                public void onFailure(Throwable e) {
                    savePending = false;
                    Timber.e("Failed to fetch elevation");
                    handler.post(() -> binding.landingStatus.setText("Fetch elevation failed"));
                }
            });
        } else {
            Timber.e("Failed to get map center");
        }
    }

    private void save() {
        final LandingZone lz = getLandingZone();
        if (!Services.bluetooth.isConnected()) {
            binding.landingStatus.setText(Services.bluetooth.getBtString() + " not connected");
        } else if (lz != null) {
            Timber.i("Setting landing zone %s", lz);
            ApLandingZone.setPending(lz);
            Services.bluetooth.actions.setLandingZone(lz);
            // TODO: Show pending, and wait for confirmation
            binding.landingStatus.setText("");
            getParentFragmentManager().popBackStack();
        } else {
            Timber.w("Invalid landing zone");
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
