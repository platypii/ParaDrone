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

import static ws.baseline.paradrone.bluetooth.AutopilotActions.MODE_AP;
import static ws.baseline.paradrone.bluetooth.AutopilotActions.MODE_IDLE;

public class ControlFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ControlFragmentBinding binding = ControlFragmentBinding.inflate(inflater, container, false);

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
        binding.setModeIdle.setOnClickListener((e) -> {
            Services.bluetooth.actions.setMode(MODE_IDLE);
        });
        binding.setModeAp.setOnClickListener((e) -> {
            Services.bluetooth.actions.setMode(MODE_AP);
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewState.setMode(ViewState.ViewMode.CTRL);
        EventBus.getDefault().register(this);
        checkConnected();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onBluetoothState(BluetoothState event) {
        checkConnected();
    }

    /**
     * If not connected, return to home fragment
     */
    private void checkConnected() {
        if (!Services.bluetooth.isConnected()) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.control_plane, new HomeFragment())
                    .commit();
        }
    }
}
