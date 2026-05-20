package com.rtometer.gps;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DebugPrefs {

    private static final String PREFS = "rtometer";
    static final String KEY_DEBUG_MODE = "debug_mode";
    private static final String KEY_NEXT_FIX_MS = "debug_next_fix_ms";
    private static final String KEY_LAST_REJECTION = "debug_last_rejection";
    private static final int FIX_COUNT = 3;

    public static class FixEntry {
        public final float lat, lng, distMeters;
        public final long timestampMs;

        FixEntry(float lat, float lng, float distMeters, long timestampMs) {
            this.lat = lat;
            this.lng = lng;
            this.distMeters = distMeters;
            this.timestampMs = timestampMs;
        }
    }

    public static boolean isDebugMode(Context context) {
        return prefs(context).getBoolean(KEY_DEBUG_MODE, false);
    }

    public static void setDebugMode(Context context, boolean on) {
        prefs(context).edit().putBoolean(KEY_DEBUG_MODE, on).apply();
    }

    /** Pushes a new fix into the 3-entry ring buffer (most recent first). */
    public static void pushFix(Context context, double lat, double lng,
                                float minDistMeters, long nextFixMs) {
        SharedPreferences p = prefs(context);
        SharedPreferences.Editor ed = p.edit();

        // Shift existing entries down
        for (int i = FIX_COUNT - 1; i > 0; i--) {
            ed.putFloat(latKey(i), p.getFloat(latKey(i - 1), Float.NaN));
            ed.putFloat(lngKey(i), p.getFloat(lngKey(i - 1), Float.NaN));
            ed.putFloat(distKey(i), p.getFloat(distKey(i - 1), -1f));
            ed.putLong(tsKey(i), p.getLong(tsKey(i - 1), 0));
        }

        ed.putFloat(latKey(0), (float) lat);
        ed.putFloat(lngKey(0), (float) lng);
        ed.putFloat(distKey(0), minDistMeters);
        ed.putLong(tsKey(0), System.currentTimeMillis());
        ed.putLong(KEY_NEXT_FIX_MS, nextFixMs);
        ed.remove(KEY_LAST_REJECTION);
        ed.apply();
    }

    /** Returns up to 3 fixes, most recent first. Empty slots are omitted. */
    public static List<FixEntry> getFixes(Context context) {
        SharedPreferences p = prefs(context);
        List<FixEntry> result = new ArrayList<>();
        for (int i = 0; i < FIX_COUNT; i++) {
            float lat = p.getFloat(latKey(i), Float.NaN);
            if (Float.isNaN(lat)) break;
            result.add(new FixEntry(
                    lat,
                    p.getFloat(lngKey(i), Float.NaN),
                    p.getFloat(distKey(i), -1f),
                    p.getLong(tsKey(i), 0)));
        }
        return result;
    }

    public static void saveRejection(Context context, String reason) {
        prefs(context).edit().putString(KEY_LAST_REJECTION, reason).apply();
    }

    /** Returns the last rejection reason, or null if the last attempt succeeded. */
    @Nullable
    public static String getLastRejection(Context context) {
        return prefs(context).getString(KEY_LAST_REJECTION, null);
    }

    /** Updates only the next-fix estimate without pushing a new fix entry. */
    public static void saveNextFixMs(Context context, long nextFixMs) {
        prefs(context).edit().putLong(KEY_NEXT_FIX_MS, nextFixMs).apply();
    }

    /** Returns the estimated next fix time in ms, or 0 if not set. */
    public static long getNextFixMs(Context context) {
        return prefs(context).getLong(KEY_NEXT_FIX_MS, 0);
    }

    private static String latKey(int i)  { return "debug_fix_" + i + "_lat"; }
    private static String lngKey(int i)  { return "debug_fix_" + i + "_lng"; }
    private static String distKey(int i) { return "debug_fix_" + i + "_dist"; }
    private static String tsKey(int i)   { return "debug_fix_" + i + "_ts"; }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
