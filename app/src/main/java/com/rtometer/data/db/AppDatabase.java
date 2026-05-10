package com.rtometer.data.db;

import android.content.Context;

import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Quarter.class, Office.class, AttendanceDay.class, AppConfig.class, BankHoliday.class}, version = 4, exportSchema = false)
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
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build();
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

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_attendance_days_detectedOfficeId " +
                "ON attendance_days (detectedOfficeId)"
            );
        }
    };

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

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE app_config ADD COLUMN fiscalQuarterPreset TEXT");
            database.execSQL("ALTER TABLE app_config ADD COLUMN customStartMonth INTEGER NOT NULL DEFAULT 1");
        }
    };
}
