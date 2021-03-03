package ws.baseline.paradrone.map;

import timber.log.Timber;
import ws.baseline.paradrone.R;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.ElevationResult;

public class Elevation {

    public static void get(@NonNull Context context, @NonNull LatLng ll, PendingResult.Callback<ElevationResult> cb) {
        try {
            final GeoApiContext ctx = new GeoApiContext.Builder()
                    .apiKey(context.getString(R.string.google_elevation_key))
                    .build();
            ElevationApi.getByPoint(ctx, new com.google.maps.model.LatLng(ll.latitude, ll.longitude))
                    .setCallback(cb);
        } catch (Throwable e) {
            Timber.e(e);
        }
    }
}
