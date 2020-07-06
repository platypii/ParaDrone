package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.BluetoothState;
import ws.baseline.paradrone.databinding.ControlFragmentBinding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class ControlFragment extends Fragment {

    private ControlFragmentBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ControlFragmentBinding.inflate(inflater, container, false);

        binding.buttonUp.setOnClickListener((e) -> {
            Services.bluetooth.actions.setMotorSpeed(-127, -127);
        });
        binding.buttonUpLeft.setOnClickListener((e) -> {
            Services.bluetooth.actions.setMotorSpeed(-127, 0);
        });
        binding.buttonUpRight.setOnClickListener((e) -> {
            Services.bluetooth.actions.setMotorSpeed(0, -127);
        });
        binding.buttonDown.setOnClickListener((e) -> {
            Services.bluetooth.actions.setMotorPosition(255, 255);
        });
        update();
        return binding.getRoot();
    }

    private void update() {
        final boolean connected = Services.bluetooth.isConnected();
        binding.buttonUp.setEnabled(connected);
        binding.buttonUpRight.setEnabled(connected);
        binding.buttonUpLeft.setEnabled(connected);
        binding.buttonDown.setEnabled(connected);
        binding.controlView.setEnabled(connected);
        binding.controlFragment.setAlpha(connected ? 1 : 0.5f);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onBluetoothState(BluetoothState event) {
        update();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
