package com.rtometer.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface BankHolidayDao {

    @Insert
    long insert(BankHoliday holiday);

    @Insert
    void insertAll(List<BankHoliday> holidays);

    @Delete
    void delete(BankHoliday holiday);

    @Query("DELETE FROM bank_holidays WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM bank_holidays WHERE countryCode = :countryCode")
    void deleteByCountryCode(String countryCode);

    @Query("SELECT * FROM bank_holidays WHERE year = :year ORDER BY date ASC")
    List<BankHoliday> getByYear(int year);

    @Query("SELECT date FROM bank_holidays WHERE year = :year ORDER BY date ASC")
    List<LocalDate> getDatesForYear(int year);
}
