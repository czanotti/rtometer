package com.rtometer.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface AppConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(AppConfig c);

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    AppConfig get();

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    LiveData<AppConfig> observe();
}
