package com.rtometer.data.db;

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

    @Query("SELECT * FROM quarters WHERE fiscalYear = :fy AND quarterNumber = :qn LIMIT 1")
    Quarter getByFiscalYearAndNumber(int fy, int qn);

    @Query("SELECT * FROM quarters ORDER BY fiscalYear DESC, quarterNumber DESC")
    List<Quarter> getAll();
}
