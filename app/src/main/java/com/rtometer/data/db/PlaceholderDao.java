package com.rtometer.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlaceholderDao {
    @Insert
    void insert(PlaceholderEntity entity);

    @Query("SELECT * FROM placeholder")
    List<PlaceholderEntity> getAll();
}
