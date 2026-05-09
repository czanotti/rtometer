package com.rtometer.data.db;

import androidx.room.TypeConverter;

import com.rtometer.data.model.DayStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class Converters {

    @TypeConverter
    public static String fromLocalDate(LocalDate value) {
        return value == null ? null : value.toString();
    }

    @TypeConverter
    public static LocalDate toLocalDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    @TypeConverter
    public static String fromLocalTime(LocalTime value) {
        return value == null ? null : value.toString();
    }

    @TypeConverter
    public static LocalTime toLocalTime(String value) {
        return value == null ? null : LocalTime.parse(value);
    }

    @TypeConverter
    public static String fromDayStatus(DayStatus value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static DayStatus toDayStatus(String value) {
        return value == null ? null : DayStatus.valueOf(value);
    }
}
