package com.rtometer;

import android.app.Application;

import com.rtometer.data.db.AppConfig;
import com.rtometer.data.db.AppDatabase;
import com.rtometer.gps.GpsScheduler;

import java.util.concurrent.Executors;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class RTOmeterApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            AppConfig cfg = db.appConfigDao().get();
            int interval = cfg != null ? cfg.gpsIntervalMinutes : 120;
            GpsScheduler.schedule(this, interval);
        });
    }
}
