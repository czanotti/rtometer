package com.rtometer.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Quarter.class, Office.class, AttendanceDay.class, AppConfig.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract QuarterDao quarterDao();
    public abstract OfficeDao officeDao();
    public abstract AttendanceDayDao attendanceDayDao();
    public abstract AppConfigDao appConfigDao();
}
