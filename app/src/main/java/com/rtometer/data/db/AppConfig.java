package com.rtometer.data.db;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalTime;

@Entity(tableName = "app_config")
public class AppConfig {
    @PrimaryKey
    public int id;
    public LocalTime workDayStart = LocalTime.of(8, 0);
    public LocalTime workDayEnd = LocalTime.of(18, 0);
    public int gpsIntervalMinutes = 15;
    @Nullable
    public String bankHolidayCountry;
    public int fiscalYearOffset = 1;
}
