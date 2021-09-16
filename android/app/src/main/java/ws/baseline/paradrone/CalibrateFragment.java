package ws.baseline.paradrone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import ws.baseline.paradrone.bluetooth.ApCalibrationMsg;
import ws.baseline.paradrone.bluetooth.BluetoothState;
import ws.baseline.paradrone.databinding.CalibrateFragmentBinding;

/**
 * Calibration fragment.
 */
public class CalibrateFragment extends Fragment {
    private CalibrateFragmentBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CalibrateFragmentBinding.inflate(inflater, container, false);
        binding.calibrate.setOnClickListener((e) -> Services.bluetooth.actions.calibrate());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewState.setMode(ViewState.ViewMode.CFG);
        EventBus.getDefault().register(this);
        onBluetoothState(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onBluetoothState(@Nullable BluetoothState bt) {
        binding.calibrate.setEnabled(Services.bluetooth.isConnected());
    }

    @Subscribe
    public void onCalibration(@NonNull ApCalibrationMsg cal) {
        binding.calibrateLeft1.setText(String.format(Locale.getDefault(), "%d", cal.left1));
        binding.calibrateLeft2.setText(String.format(Locale.getDefault(), "%d", cal.left2));
        binding.calibrateRight1.setText(String.format(Locale.getDefault(), "%d", cal.right1));
        binding.calibrateRight2.setText(String.format(Locale.getDefault(), "%d", cal.right2));

        // TODO: Warn if delta > 10%
    }

}
