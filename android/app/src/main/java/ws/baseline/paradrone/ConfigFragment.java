package ws.baseline.paradrone;

import ws.baseline.paradrone.databinding.ConfigFragmentBinding;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ConfigFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final ConfigFragmentBinding binding = ConfigFragmentBinding.inflate(inflater, container, false);

        binding.setLandingZone.setOnClickListener((e) -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.control_plane, new LandingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.setFrequency.setOnClickListener((e) -> {
            final EditText edit = new EditText(getContext());
            edit.setInputType(InputType.TYPE_CLASS_NUMBER);
            edit.setText("915000000");
            new AlertDialog.Builder(getContext())
                    .setTitle("Frequency")
                    .setView(edit)
                    .setPositiveButton(android.R.string.ok, (d, which) -> {
                        final int freq = Integer.parseInt(edit.getText().toString());
                        Services.bluetooth.actions.setFrequency(freq);
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewState.setMode(ViewState.ViewMode.CFG);
    }
}
