package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.ApConfigMsg;
import ws.baseline.paradrone.databinding.ConfigFragmentBinding;

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

import static androidx.core.content.ContextCompat.getDrawable;

/**
 * Autopilot configuration. Frequency, stroke length, motor direction.
 */
public class ApConfigFragment extends Fragment {
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
        binding.cfgCancel.setOnClickListener((e) -> getParentFragmentManager().popBackStack());

        // Initiate request for current status
        Services.bluetooth.actions.fetchConfig();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewState.setMode(ViewState.ViewMode.CFG);
        EventBus.getDefault().register(this);
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
        binding.cfgTop.setText(String.format(Locale.getDefault(), "%d", msg.top));
        binding.cfgStall.setText(String.format(Locale.getDefault(), "%d", msg.stall));
        left = msg.left();
        right = msg.right();
        updateLeftRight();
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

    private void send() {
        // Load from form
        final int frequency = Integer.parseInt(binding.cfgFreq.getText().toString());
        final short top = Short.parseShort(binding.cfgTop.getText().toString());
        final short stall = Short.parseShort(binding.cfgStall.getText().toString());
        final byte dir = (byte) ((left ? 1 : 0) + (right ? 2 : 0));
        final ApConfigMsg msg = new ApConfigMsg(frequency, top, stall, dir);
        Services.bluetooth.actions.setConfig(msg);
    }
}
