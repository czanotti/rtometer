package com.rtometer.data.db;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rtometer.data.model.DayStatus;

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
public class AttendanceDayDaoTest {
    private AppDatabase db;
    private AttendanceDayDao dao;
    private QuarterDao quarterDao;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.attendanceDayDao();
        quarterDao = db.quarterDao();
    }

    @After
    public void teardown() {
        db.close();
    }

    private long insertQuarter(int fy, int qn) {
        Quarter q = new Quarter();
        q.quarterNumber = qn;
        q.startDate = LocalDate.of(fy, 1, 1);
        q.endDate = LocalDate.of(fy, 3, 31);
        return quarterDao.insert(q);
    }

    @Test
    public void insertAndGetByQuarterId() {
        long qId1 = insertQuarter(2025, 1);
        long qId2 = insertQuarter(2025, 2);

        AttendanceDay d1 = new AttendanceDay();
        d1.date = LocalDate.of(2025, 1, 6);
        d1.quarterId = qId1;
        d1.status = DayStatus.IN_OFFICE;

        AttendanceDay d2 = new AttendanceDay();
        d2.date = LocalDate.of(2025, 1, 7);
        d2.quarterId = qId1;
        d2.status = DayStatus.NOT_IN_OFFICE;

        AttendanceDay d3 = new AttendanceDay();
        d3.date = LocalDate.of(2025, 4, 1);
        d3.quarterId = qId2;
        d3.status = DayStatus.IN_OFFICE;

        dao.insert(d1);
        dao.insert(d2);
        dao.insert(d3);

        List<AttendanceDay> result = dao.getByQuarterId(qId1);
        assertEquals(2, result.size());
    }

    @Test
    public void getByDate() {
        long qId = insertQuarter(2025, 1);

        AttendanceDay d = new AttendanceDay();
        d.date = LocalDate.of(2025, 1, 15);
        d.quarterId = qId;
        d.status = DayStatus.IN_OFFICE;
        dao.insert(d);

        AttendanceDay result = dao.getByDate("2025-01-15");
        assertNotNull(result);
        assertEquals(DayStatus.IN_OFFICE, result.status);
    }

    @Test
    public void update_changesStatus() {
        long qId = insertQuarter(2025, 1);

        AttendanceDay d = new AttendanceDay();
        d.date = LocalDate.of(2025, 1, 6);
        d.quarterId = qId;
        d.status = DayStatus.IN_OFFICE;
        long id = dao.insert(d);

        List<AttendanceDay> days = dao.getByQuarterId(qId);
        AttendanceDay fetched = days.get(0);
        fetched.status = DayStatus.SICK;
        dao.update(fetched);

        AttendanceDay updated = dao.getByDate("2025-01-06");
        assertEquals(DayStatus.SICK, updated.status);
    }

    @Test
    public void deleteByQuarterId() {
        long qId = insertQuarter(2025, 1);

        AttendanceDay d1 = new AttendanceDay();
        d1.date = LocalDate.of(2025, 1, 6);
        d1.quarterId = qId;
        d1.status = DayStatus.IN_OFFICE;

        AttendanceDay d2 = new AttendanceDay();
        d2.date = LocalDate.of(2025, 1, 7);
        d2.quarterId = qId;
        d2.status = DayStatus.NOT_IN_OFFICE;

        dao.insert(d1);
        dao.insert(d2);

        dao.deleteByQuarterId(qId);

        List<AttendanceDay> result = dao.getByQuarterId(qId);
        assertTrue(result.isEmpty());
    }
}
