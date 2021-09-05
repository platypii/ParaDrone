package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.ApConfigMsg;
import ws.baseline.paradrone.bluetooth.BluetoothState;
import ws.baseline.paradrone.databinding.ConfigFragmentBinding;
import ws.baseline.paradrone.util.Numbers;

import android.content.Context;
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
import timber.log.Timber;

import static androidx.core.content.ContextCompat.getDrawable;

/**
 * Autopilot configuration. Frequency, stroke length, motor direction.
 */
public class ConfigFragment extends Fragment {
    private ConfigFragmentBinding binding;

    private boolean left = false;
    private boolean right = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ConfigFragmentBinding.inflate(inflater, container, false);
        binding.cfgLeft.setOnClickListener((e) -> {
            left = !left;
            updateLeftRight();
        });
        binding.cfgRight.setOnClickListener((e) -> {
            right = !right;
            updateLeftRight();
        });
        binding.cfgSend.setOnClickListener((e) -> send());

        // Initiate request for current status
        Services.bluetooth.actions.fetchConfig();

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
    public void onConfigMsg(@NonNull ApConfigMsg msg) {
        // Update views
        binding.cfgFreq.setText(String.format(Locale.getDefault(), "%d", msg.frequency));
        binding.cfgStroke.setText(String.format(Locale.getDefault(), "%d", msg.stroke));
        left = msg.left();
        right = msg.right();
        updateLeftRight();
    }

    @Subscribe
    public void onBluetoothState(@Nullable BluetoothState bt) {
        binding.cfgSend.setEnabled(Services.bluetooth.isConnected());
    }

    private void updateLeftRight() {
        final Context ctx = getContext();
        if (ctx != null) {
            final int leftIcon = left ? R.drawable.counterclockwise : R.drawable.clockwise;
            final int rightIcon = right ? R.drawable.counterclockwise : R.drawable.clockwise;
            binding.cfgLeft.setImageDrawable(getDrawable(ctx, leftIcon));
            binding.cfgRight.setImageDrawable(getDrawable(ctx, rightIcon));
        }
    }

    /**
     * Validate, parse form, and send to device
     */
    private void send() {
        try {
            // Load form
            final int frequency = Numbers.parseInt(binding.cfgFreq.getText().toString(), -1);
            final int stroke = Numbers.parseInt(binding.cfgStroke.getText().toString(), -1);
            final byte dir = (byte) ((left ? 1 : 0) + (right ? 2 : 0));

            // Validate
            if (frequency < 100000000 || frequency > 999000000) {
                binding.cfgFreq.setError("100000000 to 999000000 Hz");
                binding.cfgFreq.requestFocus();
                return;
            }
            if (stroke < 0 || stroke > 4000) {
                binding.cfgStroke.setError("0 to 4000 mm");
                binding.cfgStroke.requestFocus();
                return;
            }

            final ApConfigMsg msg = new ApConfigMsg(frequency, (short) stroke, dir);
            Services.bluetooth.actions.setConfig(msg);
        } catch (NumberFormatException e) {
            Timber.e(e);
        }
    }
}
