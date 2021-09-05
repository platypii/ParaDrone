package ws.baseline.paradrone.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import timber.log.Timber;

public class ApConfigMsg implements ApEvent {
    public final int frequency;
    public final short stroke; // mm
    public final byte dir;

    @Nullable
    public static ApConfigMsg lastCfg;

    public ApConfigMsg(int frequency, short stroke, byte dir) {
        this.frequency = frequency;
        this.stroke = stroke;
        this.dir = dir;
    }

     public static void parse(@NonNull byte[] value) {
        if (value[0] == 'C') {
            // 'C', freq, top, stall, dir
            final ByteBuffer buf = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
            final int freq = buf.getInt(1);
            final short stroke = buf.getShort(5);
            final byte dir = buf.get(7);
            lastCfg = new ApConfigMsg(freq, stroke, dir);
            Timber.i("ap -> phone: cfg %s", lastCfg);
            EventBus.getDefault().post(lastCfg);
        } else {
            Timber.e("Unexpected config message: %s", value[0]);
        }
    }

    @NonNull
    public byte[] toBytes() {
        // Pack frequency into bytes
        final byte[] bytes = new byte[8];
        bytes[0] = 'C';
        final ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(1, frequency);
        buf.putShort(5, stroke);
        buf.put(7, dir);
        return bytes;
    }

    /**
     * Left motor clockwise?
     */
    public final boolean left() {
        return (dir & 1) != 0;
    }

    /**
     * Right motor clockwise?
     */
    public final boolean right() {
        return (dir & 2) != 0;
    }

    @NonNull
    @Override
    public String toString() {
        final double f = frequency * 1e-6;
        final char l = left() ? '↑' : '↓';
        final char r = right() ? '↑' : '↓';
        return String.format(Locale.getDefault(), "C %f MHz, %d, %c, %c", f, stroke, l, r);
    }
}
