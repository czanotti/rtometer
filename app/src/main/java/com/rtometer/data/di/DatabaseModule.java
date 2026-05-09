package com.rtometer.data.di;

import android.content.Context;

import androidx.room.Room;

import com.rtometer.data.db.AppConfigDao;
import com.rtometer.data.db.AppDatabase;
import com.rtometer.data.db.AttendanceDayDao;
import com.rtometer.data.db.OfficeDao;
import com.rtometer.data.db.QuarterDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "rtometer.db").build();
    }

    @Provides
    public QuarterDao provideQuarterDao(AppDatabase db) {
        return db.quarterDao();
    }

    @Provides
    public OfficeDao provideOfficeDao(AppDatabase db) {
        return db.officeDao();
    }

    @Provides
    public AttendanceDayDao provideAttendanceDayDao(AppDatabase db) {
        return db.attendanceDayDao();
    }

    @Provides
    public AppConfigDao provideAppConfigDao(AppDatabase db) {
        return db.appConfigDao();
    }
}
