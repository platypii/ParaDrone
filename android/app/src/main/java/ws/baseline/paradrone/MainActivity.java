package ws.baseline.paradrone;

import android.content.Intent;
import androidx.annotation.Nullable;
import ws.baseline.paradrone.bluetooth.BluetoothService;
import ws.baseline.paradrone.databinding.ActivityMainBinding;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_LOCATION = 2010;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Timber.plant(new Timber.DebugTree());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Location is needed for map location and bluetooth
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }

        binding.setConfig.setOnClickListener((e) -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.control_plane, new OptionsFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Subscribe
    public void onViewState(ViewState.ViewMode mode) {
        if (mode == ViewState.ViewMode.CFG) {
            binding.landingArrow.setVisibility(View.GONE);
            binding.setConfig.setVisibility(View.GONE);
        } else if (mode == ViewState.ViewMode.CTRL) {
            binding.landingArrow.setVisibility(View.GONE);
            binding.setConfig.setVisibility(View.VISIBLE);
        } else if (mode == ViewState.ViewMode.LZ) {
            binding.landingArrow.setVisibility(View.VISIBLE);
            binding.setConfig.setVisibility(View.GONE);
        } else {
            binding.landingArrow.setVisibility(View.GONE);
            binding.setConfig.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == BluetoothService.ENABLE_BLUETOOTH_CODE) {
            Timber.i("Bluetooth enabled");
            Services.bluetooth.start(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Services.start(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Services.stop();
        EventBus.getDefault().unregister(this);
    }
}
