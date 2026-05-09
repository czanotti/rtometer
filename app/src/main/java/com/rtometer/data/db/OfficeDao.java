package com.rtometer.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface OfficeDao {
    @Insert
    long insert(Office o);

    @Update
    void update(Office o);

    @Delete
    void delete(Office o);

    @Query("SELECT * FROM offices")
    List<Office> getAll();

    @Query("SELECT * FROM offices WHERE isPrimary = 1 LIMIT 1")
    Office getPrimary();
}
