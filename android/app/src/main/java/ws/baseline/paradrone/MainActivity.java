package ws.baseline.paradrone;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.annotation.NonNull;
import ws.baseline.paradrone.bluetooth.BluetoothService;
import ws.baseline.paradrone.databinding.ActivityMainBinding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Timber.plant(new Timber.DebugTree());

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Permissions.RC_BLUE) {
            // Check for all permissions granted
            int success = 0;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    success++;
                }
            }
            if (permissions.length == success) {
                Timber.i("Bluetooth permission granted");
                // Start bluetooth again
                Services.bluetooth.start(this);
            } else {
                Timber.w("Bluetooth permission not granted");
            }
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
