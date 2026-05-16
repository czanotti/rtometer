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

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class QuarterDaoTest {
    private AppDatabase db;
    private QuarterDao dao;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.quarterDao();
    }

    @After
    public void teardown() {
        db.close();
    }

    @Test
    public void insertAndGetById() {
        Quarter q = new Quarter();
        q.quarterNumber = 1;
        q.startDate = LocalDate.of(2026, 1, 1);
        q.endDate = LocalDate.of(2026, 3, 31);
        long id = dao.insert(q);
        Quarter result = dao.getById(id);
        assertEquals(1, result.quarterNumber);
        assertEquals(LocalDate.of(2026, 1, 1), result.startDate);
    }

    @Test
    public void getAll_orderedByStartDateDesc() {
        Quarter q1 = new Quarter();
        q1.quarterNumber = 1;
        q1.startDate = LocalDate.of(2026, 1, 1);
        q1.endDate = LocalDate.of(2026, 3, 31);

        Quarter q2 = new Quarter();
        q2.quarterNumber = 2;
        q2.startDate = LocalDate.of(2026, 4, 1);
        q2.endDate = LocalDate.of(2026, 6, 30);

        dao.insert(q1);
        dao.insert(q2);

        List<Quarter> all = dao.getAll();
        assertEquals(2, all.size());
        assertEquals(2, all.get(0).quarterNumber);
    }

    @Test
    public void getByDate_returnsCorrectQuarter() {
        Quarter q1 = new Quarter();
        q1.quarterNumber = 1;
        q1.startDate = LocalDate.of(2026, 1, 1);
        q1.endDate = LocalDate.of(2026, 3, 31);

        Quarter q2 = new Quarter();
        q2.quarterNumber = 2;
        q2.startDate = LocalDate.of(2026, 4, 1);
        q2.endDate = LocalDate.of(2026, 6, 30);

        dao.insert(q1);
        dao.insert(q2);

        Quarter result = dao.getByDate(LocalDate.of(2026, 5, 1).toString());
        assertNotNull(result);
        assertEquals(2, result.quarterNumber);
    }

    @Test
    public void update_modifiesTargetPercentage() {
        Quarter q = new Quarter();
        q.quarterNumber = 1;
        q.startDate = LocalDate.of(2026, 1, 1);
        q.endDate = LocalDate.of(2026, 3, 31);
        q.targetPercentage = 0.5f;
        long id = dao.insert(q);

        Quarter fetched = dao.getById(id);
        fetched.targetPercentage = 0.8f;
        dao.update(fetched);

        Quarter updated = dao.getById(id);
        assertEquals(0.8f, updated.targetPercentage, 0.001f);
    }

    @Test
    public void delete_removesQuarter() {
        Quarter q = new Quarter();
        q.quarterNumber = 1;
        q.startDate = LocalDate.of(2026, 1, 1);
        q.endDate = LocalDate.of(2026, 3, 31);
        long id = dao.insert(q);

        Quarter fetched = dao.getById(id);
        dao.delete(fetched);

        assertNull(dao.getById(id));
    }
}
