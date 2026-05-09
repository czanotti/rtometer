package com.rtometer.data.di;

import android.content.Context;

import androidx.room.Room;

import com.rtometer.data.db.AppDatabase;
import com.rtometer.data.db.PlaceholderDao;

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
    public PlaceholderDao providePlaceholderDao(AppDatabase db) {
        return db.placeholderDao();
    }
}
