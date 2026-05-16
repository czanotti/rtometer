package com.rtometer.data.db;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalTime;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AppConfigDaoTest {
    private AppDatabase db;
    private AppConfigDao dao;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.appConfigDao();
    }

    @After
    public void teardown() {
        db.close();
    }

    @Test
    public void get_returnsNullWhenEmpty() {
        assertNull(dao.get());
    }

    @Test
    public void upsert_insertsConfig() {
        AppConfig config = new AppConfig();
        config.id = 1;
        config.workDayStart = LocalTime.of(8, 0);
        config.workDayEnd = LocalTime.of(18, 0);
        config.gpsIntervalMinutes = 30;
        dao.upsert(config);

        AppConfig result = dao.get();
        assertNotNull(result);
        assertEquals(30, result.gpsIntervalMinutes);
    }

    @Test
    public void upsert_replacesExistingConfig() {
        AppConfig config1 = new AppConfig();
        config1.id = 1;
        config1.workDayStart = LocalTime.of(8, 0);
        config1.workDayEnd = LocalTime.of(18, 0);
        config1.gpsIntervalMinutes = 60;
        dao.upsert(config1);

        AppConfig config2 = new AppConfig();
        config2.id = 1;
        config2.workDayStart = LocalTime.of(9, 0);
        config2.workDayEnd = LocalTime.of(17, 0);
        config2.gpsIntervalMinutes = 120;
        dao.upsert(config2);

        AppConfig result = dao.get();
        assertEquals(120, result.gpsIntervalMinutes);
    }
}
