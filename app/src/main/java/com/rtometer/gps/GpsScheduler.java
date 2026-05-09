package com.rtometer.gps;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class GpsScheduler {

    static final String WORK_TAG = "gps_detection";

    public static void schedule(Context context, int intervalMinutes) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                GpsDetectionWorker.class,
                intervalMinutes, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.REPLACE, request);
    }

    public static void cancelToday(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG);
    }
}
