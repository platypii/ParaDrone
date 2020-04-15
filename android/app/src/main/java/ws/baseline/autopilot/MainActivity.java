package ws.baseline.autopilot;

import ws.baseline.autopilot.databinding.ActivityMainBinding;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";

    private static final int PERMISSION_REQUEST_LOCATION = 2010;
    private ActivityMainBinding binding;

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
            binding.crosshairOverlay.setVisibility(View.VISIBLE);
            // TODO
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
