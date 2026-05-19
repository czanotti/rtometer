package com.rtometer.gps;

import android.content.Context;
import android.content.SharedPreferences;

public class DebugPrefs {

    private static final String PREFS = "rtometer";
    static final String KEY_DEBUG_MODE = "debug_mode";
    private static final String KEY_LAST_LAT = "debug_last_lat";
    private static final String KEY_LAST_LNG = "debug_last_lng";
    private static final String KEY_MIN_DIST = "debug_min_distance_m";
    private static final String KEY_LAST_CHECK_MS = "debug_last_check_ms";

    public static boolean isDebugMode(Context context) {
        return prefs(context).getBoolean(KEY_DEBUG_MODE, false);
    }

    public static void setDebugMode(Context context, boolean on) {
        prefs(context).edit().putBoolean(KEY_DEBUG_MODE, on).apply();
    }

    public static void saveResult(Context context, double lat, double lng, float minDistanceMeters) {
        prefs(context).edit()
                .putFloat(KEY_LAST_LAT, (float) lat)
                .putFloat(KEY_LAST_LNG, (float) lng)
                .putFloat(KEY_MIN_DIST, minDistanceMeters)
                .putLong(KEY_LAST_CHECK_MS, System.currentTimeMillis())
                .apply();
    }

    public static float getLastLat(Context context) {
        return prefs(context).getFloat(KEY_LAST_LAT, Float.NaN);
    }

    public static float getLastLng(Context context) {
        return prefs(context).getFloat(KEY_LAST_LNG, Float.NaN);
    }

    /** Returns the min distance in metres, or -1 if no offices are registered. */
    public static float getMinDistance(Context context) {
        return prefs(context).getFloat(KEY_MIN_DIST, -1f);
    }

    public static long getLastCheckMs(Context context) {
        return prefs(context).getLong(KEY_LAST_CHECK_MS, 0);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
