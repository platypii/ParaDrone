package ws.baseline.paradrone;

import ws.baseline.paradrone.geo.Point;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

public class ControlView extends View {

    // x,y screen coordinates
    private Point touch1;
    private Point touch2;

    // 0 = no deflection, 255 = full deflection
    private short left_toggle;
    private short right_toggle;

    private final float density = getResources().getDisplayMetrics().density;
    private final Paint paint = new Paint();

    // Last time we sent a control message
    private long last_controls_sent;
    private static final long control_rate = 300; // ms

    public ControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setTextSize(36 * density);
        invalidate();
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        final int h = getHeight();
        final int w = getWidth();

        // Axes
        paint.setColor(0xffeeeeee);
        canvas.drawLine(0, 2, w, 2, paint); // TODO: Draw at high point margin
        canvas.drawLine(w / 2, 0, w / 2, h, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("L", 5, h - 30, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("R", w - 5, h - 30, paint);

        // Draw touches
        paint.setColor(0x33eeeeee);
        if (touch1 != null) {
            canvas.drawCircle((float) touch1.x, (float) touch1.y, 30 * density, paint);
        }
        if (touch2 != null) {
            canvas.drawCircle((float) touch2.x, (float) touch2.y, 30 * density, paint);
        }

        // Draw toggles
        final float l = (float) (left_toggle * h / 255);
        final float r = (float) (right_toggle * h / 255);
        paint.setColor(0x88eeeeee);
        canvas.drawRect(0, l - 30 * density, 15 * density, l + 30 * density, paint);
        canvas.drawRect(w - 15 * density, r - 30 * density, w, r + 30 * density, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int index = event.getActionIndex();
        final float x = event.getX(index);
        final float y = event.getY(index);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touch1 = new Point(x, y);
                updateControls();
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                touch2 = new Point(x, y);
                updateControls();
                return true;
            case MotionEvent.ACTION_MOVE:
                touch1 = new Point(event.getX(0), event.getY(0));
                if (event.getPointerCount() > 1) {
                    touch2 = new Point(event.getX(1), event.getY(1));
                }
                updateControls();
                return true;
            case MotionEvent.ACTION_UP:
                touch1 = null;
                last_controls_sent = 0; // always send touch up
                updateControls();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                touch2 = null;
                updateControls();
                return true;
            default:
                Timber.e("Unknown touch %d", event.getActionMasked());
                return super.onTouchEvent(event);
        }
    }

    /**
     * Update control settings based on touches
     */
    private void updateControls() {
        final int h = getHeight();
        final int w = getWidth();

        left_toggle = 0;
        right_toggle = 0;
        if (touch1 != null && touch2 != null) {
            if (touch1.x < w * 0.4) {
                left_toggle = (short) (255 * touch1.y / h); // 0..255
            }
            if (touch2.x < w * 0.4) {
                left_toggle = (short) (255 * touch2.y / h); // 0..255
            }
            if (w * 0.6 < touch1.x) {
                right_toggle = (short) (255 * touch1.y / h); // 0..255
            }
            if (w * 0.6 < touch2.x) {
                right_toggle = (short) (255 * touch2.y / h); // 0..255
            }
        } else if (touch1 != null) {
            // Normalize x to -1..1
            final double x_norm = touch1.x * 2 / w - 1;
            // Normalize y to 0..1
            final double y_norm = touch1.y / h;

            // ReLU
            final double left_weight = clip(0, -2.5 * x_norm + 1.5, 1);
            final double right_weight = clip(0, 2.5 * x_norm + 1.5, 1);

            left_toggle = (short) (255 * y_norm * left_weight);
            right_toggle = (short) (255 * y_norm * right_weight);
        }

        left_toggle = normalize(left_toggle);
        right_toggle = normalize(right_toggle);

        // Send control message at most every 300ms, except always send zero
        if (System.currentTimeMillis() - last_controls_sent >= control_rate || left_toggle + right_toggle == 0) {
            last_controls_sent = System.currentTimeMillis();
            Services.bluetooth.actions.setMotorPosition(left_toggle, right_toggle);
        }

        invalidate();
    }

    private static double clip(double min, double d, double max) {
        return Math.max(min, Math.min(d, max));
    }

    private static short normalize(short d) {
        return d < 0 ? 0 : d > 255 ? 255 : d;
    }
}
