package com.rtometer.gps;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.HandlerThread;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LocationUtils {

    static final long MAX_LOCATION_AGE_MS = 10L * 60 * 1000;
    static final float MAX_ACCURACY_METERS = 150f;
    private static final long FRESH_TIMEOUT_MS = 20_000;

    @Nullable
    public static Location pickBestLocation(@Nullable Location gps, @Nullable Location network) {
        long cutoff = System.currentTimeMillis() - MAX_LOCATION_AGE_MS;
        Location best = null;
        for (Location loc : new Location[]{gps, network}) {
            if (loc == null) continue;
            if (loc.getTime() <= cutoff) continue;
            if (loc.getAccuracy() > MAX_ACCURACY_METERS) continue;
            if (best == null || loc.getAccuracy() < best.getAccuracy()) {
                best = loc;
            }
        }
        return best;
    }

    /** Describes why gps/network were rejected, for debug display. */
    static String describeRejection(@Nullable Location gps, @Nullable Location network) {
        if (gps == null && network == null) return "no location cached";
        long now = System.currentTimeMillis();
        return describeOne("GPS", gps, now) + "  " + describeOne("NET", network, now);
    }

    private static String describeOne(String label, @Nullable Location loc, long now) {
        if (loc == null) return label + ":none";
        long ageSec = (now - loc.getTime()) / 1000;
        if (ageSec > MAX_LOCATION_AGE_MS / 1000) return label + ":" + (ageSec / 60) + "m old";
        if (loc.getAccuracy() > MAX_ACCURACY_METERS) return label + ":" + (int) loc.getAccuracy() + "m acc";
        return label + ":ok";
    }

    /**
     * Actively requests a fresh location fix, blocking up to 20 seconds.
     * Used as fallback when the cached location is stale or inaccurate.
     */
    @Nullable
    @SuppressWarnings("MissingPermission")
    public static Location requestFreshLocation(LocationManager lm) {
        String provider = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ? LocationManager.GPS_PROVIDER
                : LocationManager.NETWORK_PROVIDER;
        if (!lm.isProviderEnabled(provider)) return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return requestFreshApi30(lm, provider);
        }
        return requestFreshLegacy(lm, provider);
    }

    @RequiresApi(30)
    private static Location requestFreshApi30(LocationManager lm, String provider) {
        CountDownLatch latch = new CountDownLatch(1);
        Location[] result = {null};
        CancellationSignal signal = new CancellationSignal();
        try {
            lm.getCurrentLocation(provider, signal, Executors.newSingleThreadExecutor(),
                    loc -> { result[0] = loc; latch.countDown(); });
            if (!latch.await(FRESH_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                signal.cancel();
            }
        } catch (InterruptedException | SecurityException e) {
            Thread.currentThread().interrupt();
            signal.cancel();
        }
        return result[0];
    }

    @SuppressWarnings("deprecation")
    private static Location requestFreshLegacy(LocationManager lm, String provider) {
        CountDownLatch latch = new CountDownLatch(1);
        Location[] result = {null};
        HandlerThread ht = new HandlerThread("rtometer-loc");
        ht.start();
        LocationListener listener = loc -> { result[0] = loc; latch.countDown(); };
        try {
            lm.requestSingleUpdate(provider, listener, ht.getLooper());
            latch.await(FRESH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | SecurityException e) {
            Thread.currentThread().interrupt();
        } finally {
            try { lm.removeUpdates(listener); } catch (SecurityException ignored) {}
            ht.quitSafely();
        }
        return result[0];
    }

    private LocationUtils() {}
}
