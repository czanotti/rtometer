package com.rtometer.data.db;

import android.content.Context;

import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import net.sqlcipher.database.SupportFactory;

@Database(entities = {Quarter.class, Office.class, AttendanceDay.class, AppConfig.class, BankHoliday.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    byte[] passphrase = new KeystoreKeyProvider(context).getOrCreatePassphrase();
                    SupportFactory factory = new SupportFactory(passphrase);
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "rtometer.db"
                    )
                    .openHelperFactory(factory)
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
}
