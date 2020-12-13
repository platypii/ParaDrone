package ws.baseline.paradrone;

import org.greenrobot.eventbus.EventBus;
import timber.log.Timber;

/**
 * State machine for the UI
 */
class ViewState {

    enum ViewMode {
        HOME, CTRL, LZ, OPTIONS, CFG
    }

    static ViewMode mode = ViewMode.HOME;

    static void setMode(ViewMode mode) {
        if (ViewState.mode != mode) {
            Timber.i("View state %s", mode);
            ViewState.mode = mode;
            EventBus.getDefault().post(mode);
        } else {
            Timber.i("View state same same %s", mode);
        }
    }
}
