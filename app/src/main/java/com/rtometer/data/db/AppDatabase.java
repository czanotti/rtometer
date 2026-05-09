package com.rtometer.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {PlaceholderEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlaceholderDao placeholderDao();
}
