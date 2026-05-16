package com.rtometer.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface QuarterDao {
    @Insert
    long insert(Quarter q);

    @Update
    void update(Quarter q);

    @Delete
    void delete(Quarter q);

    @Query("SELECT * FROM quarters WHERE id = :id")
    Quarter getById(long id);

    @Query("SELECT * FROM quarters ORDER BY startDate DESC")
    List<Quarter> getAll();

    @Query("SELECT * FROM quarters WHERE startDate <= :date AND endDate >= :date LIMIT 1")
    Quarter getByDate(String date);

    @Query("SELECT * FROM quarters WHERE startDate <= :date AND endDate >= :date LIMIT 1")
    LiveData<Quarter> observeByDate(String date);

    @Query("DELETE FROM quarters")
    void deleteAll();
}
