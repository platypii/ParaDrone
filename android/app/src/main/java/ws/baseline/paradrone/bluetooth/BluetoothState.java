package ws.baseline.paradrone.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BluetoothState {

    public final int state;

    // Bluetooth finite state machine
    static final int BT_STOPPED = 0;
    static final int BT_STARTED = 1;
    static final int BT_SEARCHING = 2;
    static final int BT_CONNECTING = 3;
    public static final int BT_CONNECTED = 4;
    static final int BT_STOPPING = 5;

    static final String[] BT_STATES = {"stopped", "started", "searching", "connecting", "connected", "stopping"};

    BluetoothState(int state) {
        this.state = state;
    }

    public static boolean started(int state) {
        return state == BT_STARTED || state == BT_SEARCHING || state == BT_CONNECTING || state == BT_CONNECTED;
    }

    @NonNull
    public static String toString(int state) {
        return BT_STATES[state];
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof BluetoothState && ((BluetoothState) obj).state == state;
    }

    @NonNull
    @Override
    public String toString() {
        return toString(state);
    }
}
