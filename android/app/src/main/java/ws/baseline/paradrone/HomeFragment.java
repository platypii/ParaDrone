package ws.baseline.paradrone;

import ws.baseline.paradrone.bluetooth.BluetoothState;
import ws.baseline.paradrone.databinding.HomeFragmentBinding;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return HomeFragmentBinding.inflate(inflater, container, false).getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewState.setMode(ViewState.ViewMode.HOME);
        EventBus.getDefault().register(this);
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
        if (Services.bluetooth.isConnected()) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.control_plane, new ControlFragment())
                    .commit();
        }
    }
}
