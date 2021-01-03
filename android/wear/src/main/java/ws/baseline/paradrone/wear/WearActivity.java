package ws.baseline.paradrone.wear;

import ws.baseline.paradrone.Services;
import ws.baseline.paradrone.bluetooth.BluetoothState;
import ws.baseline.paradrone.wear.databinding.ActivityWearBinding;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static ws.baseline.paradrone.bluetooth.AutopilotActions.MODE_AP;
import static ws.baseline.paradrone.bluetooth.AutopilotActions.MODE_IDLE;

public class WearActivity extends Activity {
    private static final int permissionRequestCode = 1337;

    private ActivityWearBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWearBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.wearLeft.setOnClickListener((e) -> Services.bluetooth.actions.setMotorPosition(255, 0));
        binding.wearRight.setOnClickListener((e) -> Services.bluetooth.actions.setMotorPosition(0, 255));
        binding.wearIdle.setOnClickListener((e) -> Services.bluetooth.actions.setMode(MODE_IDLE));
        binding.wearAuto.setOnClickListener((e) -> Services.bluetooth.actions.setMode(MODE_AP));

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, permissionRequestCode);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothState(BluetoothState state) {
        if (state.state == BluetoothState.BT_CONNECTED) {
            binding.wearStatus.setImageResource(R.drawable.status_green);
        } else {
            binding.wearStatus.setImageResource(R.drawable.status_red);
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
    }}
