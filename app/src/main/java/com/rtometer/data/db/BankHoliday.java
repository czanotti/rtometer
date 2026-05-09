package com.rtometer.data.db;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(tableName = "bank_holidays")
public class BankHoliday {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public LocalDate date;
    @Nullable
    public String name;
    @Nullable
    public String countryCode;   // null for manually added custom holidays
    public int year;             // calendar year, for efficient querying
}
