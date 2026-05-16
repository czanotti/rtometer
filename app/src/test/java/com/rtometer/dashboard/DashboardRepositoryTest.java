package com.rtometer.dashboard;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rtometer.calculator.PaceStatus;
import com.rtometer.calculator.QuarterStats;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class DashboardRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private QuarterDao quarterDao;
    private AttendanceDayDao attendanceDayDao;
    private BankHolidayDao bankHolidayDao;
    private DashboardRepository repository;

    // A quarter that spans today so observeByDate(today) returns it
    private Quarter currentQuarter() {
        Quarter q = new Quarter();
        q.quarterNumber = 2;
        q.startDate = LocalDate.now().minusMonths(1);
        q.endDate = LocalDate.now().plusMonths(2);
        q.targetPercentage = 0.5f;
        return q;
    }

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        quarterDao = db.quarterDao();
        attendanceDayDao = db.attendanceDayDao();
        bankHolidayDao = db.bankHolidayDao();
        repository = new DashboardRepository(quarterDao, attendanceDayDao, bankHolidayDao);
    }

    @After
    public void tearDown() {
        db.close();
    }

    private QuarterStats observe() {
        AtomicReference<QuarterStats> result = new AtomicReference<>();
        Observer<QuarterStats> observer = result::set;
        repository.getStats().observeForever(observer);
        repository.getStats().removeObserver(observer);
        return result.get();
    }

    private Quarter observeQuarter() {
        AtomicReference<Quarter> result = new AtomicReference<>();
        Observer<Quarter> observer = result::set;
        repository.getCurrentQuarter().observeForever(observer);
        repository.getCurrentQuarter().removeObserver(observer);
        return result.get();
    }

    @Test
    public void noQuarter_statsIsNull() {
        assertNull(observe());
    }

    @Test
    public void noQuarter_currentQuarterIsNull() {
        assertNull(observeQuarter());
    }

    @Test
    public void withCurrentQuarter_noDays_statsComputedCorrectly() {
        long id = quarterDao.insert(currentQuarter());
        Quarter q = quarterDao.getById(id);

        QuarterStats stats = observe();

        assertNotNull(stats);
        assertEquals(0, stats.daysAttended);
        assertEquals(0, stats.daysNotInOffice);
        assertTrue(stats.totalWorkingDays > 0);
    }

    @Test
    public void withAttendanceDays_inOfficeCounted() {
        long id = quarterDao.insert(currentQuarter());
        Quarter q = quarterDao.getById(id);

        AttendanceDay d1 = new AttendanceDay();
        d1.date = LocalDate.now().minusDays(3);
        d1.quarterId = q.id;
        d1.status = DayStatus.IN_OFFICE;

        AttendanceDay d2 = new AttendanceDay();
        d2.date = LocalDate.now().minusDays(2);
        d2.quarterId = q.id;
        d2.status = DayStatus.NOT_IN_OFFICE;

        attendanceDayDao.insert(d1);
        attendanceDayDao.insert(d2);

        QuarterStats stats = observe();

        assertNotNull(stats);
        assertEquals(1, stats.daysAttended);
        assertEquals(1, stats.daysNotInOffice);
    }

    @Test
    public void paceStatus_greenWhenTargetMet() {
        Quarter q = currentQuarter();
        q.targetPercentage = 0.0f; // zero target is always met
        quarterDao.insert(q);

        QuarterStats stats = observe();

        assertNotNull(stats);
        assertEquals(PaceStatus.GREEN, stats.paceStatus);
    }

    @Test
    public void currentQuarter_returnedWhenInserted() {
        quarterDao.insert(currentQuarter());

        Quarter q = observeQuarter();

        assertNotNull(q);
        assertEquals(2, q.quarterNumber);
    }

    @Test
    public void pastQuarter_notReturnedAsCurrentQuarter() {
        Quarter past = new Quarter();
        past.quarterNumber = 1;
        past.startDate = LocalDate.of(2025, 1, 1);
        past.endDate = LocalDate.of(2025, 3, 31);
        past.targetPercentage = 0.5f;
        quarterDao.insert(past);

        assertNull(observeQuarter());
    }
}
