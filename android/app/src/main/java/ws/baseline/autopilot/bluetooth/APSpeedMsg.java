package ws.baseline.autopilot.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;

public class APSpeedMsg implements APEvent {
    public final long millis;
    public final double vN;
    public final double vE;
    public final double climb;

    @Nullable
    public static APSpeedMsg lastSpeed;

    static void update(long millis, double vN, double vE, double climb) {
        lastSpeed = new APSpeedMsg(millis, vN, vE, climb);
        EventBus.getDefault().post(lastSpeed);
    }

    private APSpeedMsg(long millis, double vN, double vE, double climb) {
        this.millis = millis;
        this.vN = vN;
        this.vE = vE;
        this.climb = climb;
    }

    public double bearing() {
        return Math.toDegrees(Math.atan2(vE, vN));
    }

    public double groundSpeed() {
        return Math.sqrt(vN * vN + vE * vE);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "S %f, %f, %.1f", vN, vE, climb);
    }
}
