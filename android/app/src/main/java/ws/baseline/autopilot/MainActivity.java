package ws.baseline.autopilot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBluetooth();
    }

    /**
     * Check if bluetooth is enabled and has been configured
     */
    private void startBluetooth() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.contains("bluetooth_address")) {
            Log.w(TAG, "Bluetooth not configured, searching...");
        }
    }
}
