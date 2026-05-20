package com.rtometer.gps;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class GpsScheduler {

    public static final int MIN_GPS_INTERVAL_MINUTES = 15;

    static final String WORK_TAG = "gps_detection";

    /** Enqueues GPS polling; skips if already running (safe to call on every app start). */
    public static void schedule(Context context, int intervalMinutes) {
        enqueue(context, intervalMinutes, ExistingPeriodicWorkPolicy.KEEP);
    }

    /** Updates GPS polling to a new interval without cancelling any in-flight run. */
    public static void reschedule(Context context, int intervalMinutes) {
        enqueue(context, intervalMinutes, ExistingPeriodicWorkPolicy.UPDATE);
    }

    private static void enqueue(Context context, int intervalMinutes,
                                 ExistingPeriodicWorkPolicy policy) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                GpsDetectionWorker.class,
                intervalMinutes, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_TAG, policy, request);
    }

    /** Runs a one-off GPS check immediately (for debug / manual trigger). */
    public static final String ONE_TIME_TAG = "gps_detection_now";

    public static void triggerNow(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(GpsDetectionWorker.class)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniqueWork(ONE_TIME_TAG, ExistingWorkPolicy.REPLACE, request);
    }

    public static void cancelToday(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG);
    }
}
