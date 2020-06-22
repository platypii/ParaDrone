package ws.baseline.paradrone.map;

import ws.baseline.paradrone.R;

import android.content.Context;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.ElevationResult;
import timber.log.Timber;

public class Elevation {

    public static void get(Context context, LatLng ll, ElevationCallback cb) {
        final GeoApiContext ctx = new GeoApiContext.Builder()
                .apiKey(context.getString(R.string.google_maps_key))
                .build();
        ElevationApi.getByPoint(ctx, new com.google.maps.model.LatLng(ll.latitude, ll.longitude)).setCallback(new PendingResult.Callback<ElevationResult>() {
            @Override
            public void onResult(ElevationResult result) {
                cb.apply(result.elevation);
            }
            @Override
            public void onFailure(Throwable e) {
                Timber.e(e, "Failed to get elevation");
            }
        });
    }

    public interface ElevationCallback {
        void apply(double elevation);
    }
}
