package ws.baseline.autopilot.bluetooth;

public class BluetoothState {

    private final int state;

    // Bluetooth finite state machine
    static final int BT_STOPPED = 0;
    static final int BT_SEARCHING = 1;
    static final int BT_CONNECTING = 2;
    public static final int BT_CONNECTED = 3;
    static final int BT_STOPPING = 4;

    static final String[] BT_STATES = {"stopped", "searching", "connecting", "connected", "stopping"};

    BluetoothState(int state) {
        this.state = state;
    }

    public static String toString(int state) {
        return BT_STATES[state];
    }
}
