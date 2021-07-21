package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.BluetoothState;
import ws.baseline.paradrone.databinding.HomeFragmentBinding;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class HomeFragment extends Fragment {
    private HomeFragmentBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);
        binding.homeText.setOnClickListener(view -> {
            // Enable bluetooth if needed
            if (!Services.bluetooth.isEnabled()) {
                final Activity activity = this.getActivity();
                if (activity != null) {
                    Services.bluetooth.enable(activity);
                }
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewState.setMode(ViewState.ViewMode.HOME);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkConnected();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onBluetoothState(BluetoothState bt) {
        checkConnected();
    }

    /**
     * If connected, change to CTRL fragment
     */
    private void checkConnected() {
        if (Services.bluetooth.isEnabled()) {
            binding.homeText.setText(R.string.bluetooth_searching);
        } else {
            binding.homeText.setText(R.string.bluetooth_disabled);
        }
        if (Services.bluetooth.isConnected()) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.control_plane, new ControlFragment())
                    .commit();
        }
    }
}
