package ws.baseline.autopilot;

import ws.baseline.autopilot.geo.Point;

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

    // Toggle control -1..1
    private double left_toggle;
    private double right_toggle;

    private final float density = getResources().getDisplayMetrics().density;
    private final Paint paint = new Paint();

    // Last time we sent a control message
    private long last_controls_sent;
    private static final long control_rate = 200; // ms

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
        canvas.drawLine(0, h / 2, w, h /2, paint);
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
        final float l = (float) (-left_toggle + 1) * h / 2;
        final float r = (float) (-right_toggle + 1) * h / 2;
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
                left_toggle = touch1.y * -2 / h + 1; // -1..1
            }
            if (touch2.x < w * 0.4) {
                left_toggle = touch2.y * -2 / h + 1; // -1..1
            }
            if (w * 0.6 < touch1.x) {
                right_toggle = touch1.y * -2 / h + 1; // -1..1
            }
            if (w * 0.6 < touch2.x) {
                right_toggle = touch2.y * -2 / h + 1; // -1..1
            }
        } else if (touch1 != null) {
            // Normalize to -1..1
            final double x_norm = touch1.x * 2 / w - 1;
            final double y_norm = touch1.y * 2 / h - 1;

            // ReLU
            final double left_weight = clip(0, -2.5 * x_norm + 1.5, 1);
            final double right_weight = clip(0, 2.5 * x_norm + 1.5, 1);

            left_toggle = -y_norm * left_weight;
            right_toggle = -y_norm * right_weight;
        }

        left_toggle = clip(-1, left_toggle, 1);
        right_toggle = clip(-1, right_toggle, 1);

        // Send control message at most every 100ms
        // TODO: Except when let go?
        if (System.currentTimeMillis() - last_controls_sent >= control_rate) {
            last_controls_sent = System.currentTimeMillis();
            final byte left_byte = (byte) (left_toggle * 127);
            final byte right_byte = (byte) (right_toggle * 127);
            Services.bluetooth.setControls(left_byte, right_byte);
        }

        invalidate();
    }

    private static double clip(double min, double d, double max) {
        return Math.max(min, Math.min(d, max));
    }
}
