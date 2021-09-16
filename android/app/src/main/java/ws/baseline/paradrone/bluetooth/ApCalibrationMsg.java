package ws.baseline.paradrone.bluetooth;

import androidx.annotation.NonNull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import timber.log.Timber;

public class ApCalibrationMsg implements ApEvent {
    public final int left1;
    public final int left2;
    public final int right1;
    public final int right2;

    static void parse(@NonNull byte[] value) {
        // 'I', left1, left2, right1, right2
        final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
        final int left1 = buf.getShort(1);
        final int left2 = buf.getShort(3);
        final int right1 = buf.getShort(5);
        final int right2 = buf.getShort(7);
        final ApCalibrationMsg lastCal = new ApCalibrationMsg(left1, left2, right1, right2);
        Timber.i("ap -> phone: calibration %s", lastCal);
        EventBus.getDefault().post(lastCal);
    }

    private ApCalibrationMsg(int left1, int left2, int right1, int right2) {
        this.left1 = left1;
        this.left2 = left2;
        this.right1 = right1;
        this.right2 = right2;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "I %d, %d, %d, %d", left1, left2, right1, right2);
    }
}
