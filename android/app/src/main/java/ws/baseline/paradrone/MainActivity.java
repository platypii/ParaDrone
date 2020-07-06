package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.APLandingZone;
import ws.baseline.paradrone.databinding.ActivityMainBinding;
import ws.baseline.paradrone.geo.LandingZone;
import ws.baseline.paradrone.map.DroneMap;
import ws.baseline.paradrone.map.Elevation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.model.LatLng;
import timber.log.Timber;

import static ws.baseline.paradrone.bluetooth.AutopilotActions.MODE_AP;
import static ws.baseline.paradrone.bluetooth.AutopilotActions.MODE_IDLE;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_LOCATION = 2010;
    private ActivityMainBinding binding;

    private boolean settingLz = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Timber.plant(new Timber.DebugTree());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }

        binding.setLandingZone.setOnClickListener((e) -> {
            if (!settingLz) {
                binding.landingArrow.setVisibility(View.VISIBLE);
                binding.setLandingZone.setText("↑ Set");
            } else {
                final DroneMap frag = (DroneMap) getSupportFragmentManager().findFragmentById(R.id.map);
                if (frag != null) {
                    final LatLng ll = frag.center();
                    final double landingDirection = frag.direction(); // radians
                    Timber.i("Getting elevation %s", ll);
                    Elevation.get(this, ll, (elevation) -> {
                        final LandingZone lz = new LandingZone(ll.latitude, ll.longitude, elevation, landingDirection);
                        Timber.i("Setting landing zone %s", lz);
                        APLandingZone.setPending(lz);
                        Services.bluetooth.actions.setLandingZone(lz);
                    });
                } else {
                    Timber.e("Failed to find map fragment");
                }
                binding.landingArrow.setVisibility(View.GONE);
                binding.setLandingZone.setText("↑ LZ");
            }
            settingLz = !settingLz;
        });

        binding.setFrequency.setOnClickListener((e) -> {
            final EditText edit = new EditText(this);
            edit.setInputType(InputType.TYPE_CLASS_NUMBER);
            edit.setText("915000000");
            new AlertDialog.Builder(this)
                    .setTitle("Frequency")
                    .setView(edit)
                    .setPositiveButton(android.R.string.ok, (d, which) -> {
                        final int freq = Integer.parseInt(edit.getText().toString());
                        Services.bluetooth.actions.setFrequency(freq);
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });

        binding.setModeIdle.setOnClickListener((e) -> {
            Services.bluetooth.actions.setMode(MODE_IDLE);
        });

        binding.setModeAp.setOnClickListener((e) -> {
            Services.bluetooth.actions.setMode(MODE_AP);
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
