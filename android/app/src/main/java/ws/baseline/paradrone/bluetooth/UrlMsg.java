package ws.baseline.paradrone.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

public class UrlMsg implements ApEvent {
    public final String url;

    @Nullable
    public static UrlMsg lastUrl;

    public UrlMsg(String url) {
        this.url = url;
    }

     public static void parse(@NonNull byte[] value) {
        if (value[0] == 'U') {
            final String url = new String(value).substring(1);
            lastUrl = new UrlMsg(url);
            Timber.i("ap -> phone: url %s", lastUrl);
            EventBus.getDefault().post(lastUrl);
        } else {
            Timber.e("Unexpected config message: %s", value[0]);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "U " + url;
    }
}
