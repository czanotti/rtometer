package com.rtometer.data.db;

import android.content.Context;

import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Quarter.class, Office.class, AttendanceDay.class, AppConfig.class, BankHoliday.class}, version = 2, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "rtometer.db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }

    @VisibleForTesting
    public static void setTestInstance(AppDatabase db) {
        INSTANCE = db;
    }

    public abstract QuarterDao quarterDao();
    public abstract OfficeDao officeDao();
    public abstract AttendanceDayDao attendanceDayDao();
    public abstract AppConfigDao appConfigDao();
    public abstract BankHolidayDao bankHolidayDao();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS bank_holidays (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "name TEXT, " +
                "countryCode TEXT, " +
                "year INTEGER NOT NULL)"
            );
        }
    };
}
