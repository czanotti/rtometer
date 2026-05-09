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

import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class OfficeDaoTest {
    private AppDatabase db;
    private OfficeDao dao;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.officeDao();
    }

    @After
    public void teardown() {
        db.close();
    }

    @Test
    public void insertAndGetAll() {
        Office o1 = new Office();
        o1.name = "HQ";
        o1.latitude = 45.0;
        o1.longitude = 9.0;
        o1.isPrimary = true;

        Office o2 = new Office();
        o2.name = "Branch";
        o2.latitude = 46.0;
        o2.longitude = 10.0;
        o2.isPrimary = false;

        dao.insert(o1);
        dao.insert(o2);

        List<Office> all = dao.getAll();
        assertEquals(2, all.size());
    }

    @Test
    public void getPrimary_returnsPrimaryOffice() {
        Office primary = new Office();
        primary.name = "HQ";
        primary.latitude = 45.0;
        primary.longitude = 9.0;
        primary.isPrimary = true;

        Office secondary = new Office();
        secondary.name = "Branch";
        secondary.latitude = 46.0;
        secondary.longitude = 10.0;
        secondary.isPrimary = false;

        dao.insert(primary);
        dao.insert(secondary);

        Office result = dao.getPrimary();
        assertNotNull(result);
        assertEquals("HQ", result.name);
    }

    @Test
    public void delete_removesOffice() {
        Office o = new Office();
        o.name = "HQ";
        o.latitude = 45.0;
        o.longitude = 9.0;
        o.isPrimary = true;
        long id = dao.insert(o);

        Office fetched = dao.getAll().get(0);
        dao.delete(fetched);

        assertEquals(0, dao.getAll().size());
    }
}
