package ws.baseline.paradrone.wear;

import ws.baseline.paradrone.Services;
import ws.baseline.paradrone.bluetooth.BluetoothState;
import ws.baseline.paradrone.wear.databinding.ActivityWearBinding;

import android.app.Activity;
import android.os.Bundle;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static ws.baseline.paradrone.bluetooth.AutopilotActions.MODE_AP;
import static ws.baseline.paradrone.bluetooth.AutopilotActions.MODE_IDLE;

public class WearActivity extends Activity {

    private ActivityWearBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWearBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.wearIdle.setOnClickListener((e) -> Services.bluetooth.actions.setMode(MODE_IDLE));
        binding.wearAuto.setOnClickListener((e) -> Services.bluetooth.actions.setMode(MODE_AP));
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
