package com.rtometer.data.db;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalTime;

@Entity(tableName = "app_config")
public class AppConfig {
    @PrimaryKey
    public int id;
    public LocalTime workDayStart = LocalTime.of(9, 30);
    public LocalTime workDayEnd = LocalTime.of(18, 0);
    public int gpsIntervalMinutes = 120;
    @Nullable
    public String fiscalQuarterPreset;
    public int customStartMonth = 2;
}
