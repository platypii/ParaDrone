package ws.baseline.autopilot.bluetooth;

public class BluetoothState {

    private final int state;

    // Bluetooth finite state machine
    static final int BT_STOPPED = 0;
    static final int BT_STARTING = 1;
    static final int BT_CONNECTING = 2;
    public static final int BT_CONNECTED = 3;
    static final int BT_DISCONNECTED = 4;
    static final int BT_STOPPING = 5;

    static final String[] BT_STATES = {"not connected", "scanning", "connecting", "connected", "disconnected", "stopping"};

    BluetoothState(int state) {
        this.state = state;
    }

    public static String toString(int state) {
        return BT_STATES[state];
    }
}
