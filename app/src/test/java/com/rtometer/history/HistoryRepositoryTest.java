package com.rtometer.history;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rtometer.data.db.AppDatabase;
import com.rtometer.data.db.AttendanceDay;
import com.rtometer.data.db.AttendanceDayDao;
import com.rtometer.data.db.BankHolidayDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.QuarterDao;
import com.rtometer.data.model.DayStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class HistoryRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private QuarterDao quarterDao;
    private AttendanceDayDao dayDao;
    private BankHolidayDao bankHolidayDao;
    private HistoryRepository repository;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        quarterDao = db.quarterDao();
        dayDao = db.attendanceDayDao();
        bankHolidayDao = db.bankHolidayDao();
        repository = new HistoryRepository(quarterDao, dayDao, bankHolidayDao);
    }

    @After
    public void tearDown() {
        db.close();
    }

    private Quarter pastQuarter(int quarterNumber) {
        Quarter q = new Quarter();
        q.quarterNumber = quarterNumber;
        q.startDate = LocalDate.of(2025, 2, 1);
        q.endDate = LocalDate.of(2025, 4, 30);
        q.targetPercentage = 0.5f;
        return q;
    }

    @Test
    public void noQuarters_returnsEmptyList() {
        assertTrue(repository.loadPastQuarters().isEmpty());
    }

    @Test
    public void currentQuarter_excluded() {
        Quarter q = new Quarter();
        q.quarterNumber = 2;
        q.startDate = LocalDate.now().minusMonths(1);
        q.endDate = LocalDate.now().plusMonths(2);
        q.targetPercentage = 0.5f;
        quarterDao.insert(q);

        assertTrue(repository.loadPastQuarters().isEmpty());
    }

    @Test
    public void pastQuarter_included() {
        quarterDao.insert(pastQuarter(1));

        List<PastQuarterEntry> result = repository.loadPastQuarters();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).quarter.quarterNumber);
    }

    @Test
    public void pastQuarter_statsIncludeAttendedDays() {
        long id = quarterDao.insert(pastQuarter(1));

        AttendanceDay d1 = new AttendanceDay();
        d1.date = LocalDate.of(2025, 2, 3);
        d1.quarterId = id;
        d1.status = DayStatus.IN_OFFICE;
        dayDao.insert(d1);

        AttendanceDay d2 = new AttendanceDay();
        d2.date = LocalDate.of(2025, 2, 4);
        d2.quarterId = id;
        d2.status = DayStatus.IN_OFFICE;
        dayDao.insert(d2);

        List<PastQuarterEntry> result = repository.loadPastQuarters();

        assertEquals(2, result.get(0).stats.daysAttended);
    }

    @Test
    public void pastQuarter_targetMetWhenSufficient() {
        Quarter q = pastQuarter(1);
        q.targetPercentage = 0.0f; // zero target is always met
        quarterDao.insert(q);

        List<PastQuarterEntry> result = repository.loadPastQuarters();
        PastQuarterEntry entry = result.get(0);

        assertTrue(entry.stats.percentage >= entry.quarter.targetPercentage);
    }

    @Test
    public void multiplePastQuarters_returnedNewestFirst() {
        Quarter q1 = new Quarter();
        q1.quarterNumber = 1;
        q1.startDate = LocalDate.of(2025, 2, 1);
        q1.endDate = LocalDate.of(2025, 4, 30);
        q1.targetPercentage = 0.5f;

        Quarter q2 = new Quarter();
        q2.quarterNumber = 2;
        q2.startDate = LocalDate.of(2025, 5, 1);
        q2.endDate = LocalDate.of(2025, 7, 31);
        q2.targetPercentage = 0.5f;

        quarterDao.insert(q1);
        quarterDao.insert(q2);

        List<PastQuarterEntry> result = repository.loadPastQuarters();

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).quarter.quarterNumber);
        assertEquals(1, result.get(1).quarter.quarterNumber);
    }

    @Test
    public void mixedQuarters_onlyPastReturned() {
        Quarter past = pastQuarter(1);
        Quarter current = new Quarter();
        current.quarterNumber = 2;
        current.startDate = LocalDate.now().minusMonths(1);
        current.endDate = LocalDate.now().plusMonths(2);
        current.targetPercentage = 0.5f;

        quarterDao.insert(past);
        quarterDao.insert(current);

        List<PastQuarterEntry> result = repository.loadPastQuarters();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).quarter.quarterNumber);
    }
}
