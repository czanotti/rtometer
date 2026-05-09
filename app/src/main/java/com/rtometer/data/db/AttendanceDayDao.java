package com.rtometer.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AttendanceDayDao {
    @Insert
    long insert(AttendanceDay d);

    @Insert
    void insertAll(List<AttendanceDay> days);

    @Update
    void update(AttendanceDay d);

    @Query("SELECT * FROM attendance_days WHERE quarterId = :quarterId")
    List<AttendanceDay> getByQuarterId(long quarterId);

    @Query("SELECT * FROM attendance_days WHERE date = :date LIMIT 1")
    AttendanceDay getByDate(String date);

    @Query("DELETE FROM attendance_days WHERE quarterId = :quarterId")
    void deleteByQuarterId(long quarterId);

    @Query("SELECT * FROM attendance_days WHERE quarterId = :quarterId")
    LiveData<List<AttendanceDay>> observeByQuarterId(long quarterId);
}
