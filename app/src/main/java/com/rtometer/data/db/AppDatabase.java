package com.rtometer.data.db;

import android.content.Context;

import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import net.sqlcipher.database.SupportFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Database(entities = {Quarter.class, Office.class, AttendanceDay.class, AppConfig.class, BankHoliday.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    dropIfUnencrypted(context, "rtometer.db");
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

    // Deletes the DB file if it is a plaintext SQLite file — handles dev installs that
    // predate SQLCipher. Safe in production because the app has no existing users.
    public static void dropIfUnencrypted(Context context, String dbName) {
        File dbFile = context.getApplicationContext().getDatabasePath(dbName);
        if (!dbFile.exists()) return;
        try (FileInputStream fis = new FileInputStream(dbFile)) {
            byte[] header = new byte[6];
            if (fis.read(header) == 6
                    && header[0] == 0x53 && header[1] == 0x51 && header[2] == 0x4c
                    && header[3] == 0x69 && header[4] == 0x74 && header[5] == 0x65) {
                context.getApplicationContext().deleteDatabase(dbName);
            }
        } catch (IOException ignored) {
        }
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
