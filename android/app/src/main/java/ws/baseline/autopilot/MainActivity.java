package ws.baseline.autopilot;

import ws.baseline.autopilot.databinding.ActivityMainBinding;
import ws.baseline.autopilot.geo.LandingZone;
import ws.baseline.autopilot.map.Elevation;
import ws.baseline.autopilot.map.MapFragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";

    private static final int PERMISSION_REQUEST_LOCATION = 2010;
    private ActivityMainBinding binding;

    private boolean settingLz = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }

        binding.setLandingZone.setOnClickListener((e) -> {
            if (!settingLz) {
                binding.landingArrow.setVisibility(View.VISIBLE);
                binding.setLandingZone.setText("✢ Set");
            } else {
                final MapFragment frag = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                if (frag != null) {
                    final LatLng ll = frag.center();
                    final double landingDirection = frag.direction(); // radians
                    Log.i(TAG, "Getting elevation " + ll);
                    Elevation.get(this, ll, (elevation) -> {
                        final LandingZone lz = new LandingZone(ll.latitude, ll.longitude, elevation, landingDirection);
                        Log.i(TAG, "Setting landing zone " + lz);
                        Services.flightComputer.lz = lz; // Set pending lz
                        Services.flightComputer.lzPending = true;
                        Services.bluetooth.setLandingZone(lz);
                    });
                } else {
                    Log.e(TAG, "Failed to find map fragment");
                }
                binding.landingArrow.setVisibility(View.GONE);
                binding.setLandingZone.setText("✢ LZ");
            }
            settingLz = !settingLz;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Services.start(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Services.stop();
    }
}
