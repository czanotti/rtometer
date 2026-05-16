package com.rtometer.ui.calendar;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rtometer.data.db.AppDatabase;
import com.rtometer.data.db.AttendanceDay;
import com.rtometer.data.db.AttendanceDayDao;
import com.rtometer.data.db.BankHoliday;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class CalendarViewModelTest {

    @Rule
    public InstantTaskExecutorRule taskRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private AttendanceDayDao dayDao;
    private QuarterDao quarterDao;
    private BankHolidayDao bankHolidayDao;
    private CalendarViewModel vm;

    @Before
    public void setup() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dayDao = db.attendanceDayDao();
        quarterDao = db.quarterDao();
        bankHolidayDao = db.bankHolidayDao();
        vm = new CalendarViewModel(dayDao, quarterDao, bankHolidayDao);
    }

    @After
    public void teardown() {
        vm.onCleared();
        db.close();
    }

    // ── buildMonths — structure ───────────────────────────────────────────────

    @Test
    public void buildMonths_threeMonthQuarter_hasThreeMonths() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        List<CalendarMonth> months = CalendarViewModel.buildMonths(q, Collections.emptyList(), Collections.emptySet());
        assertEquals(3, months.size());
        assertEquals(YearMonth.of(2025, 1), months.get(0).month);
        assertEquals(YearMonth.of(2025, 2), months.get(1).month);
        assertEquals(YearMonth.of(2025, 3), months.get(2).month);
    }

    @Test
    public void buildMonths_monthStartingMonday_hasNoLeadingNulls() {
        // Find a month that starts on Monday: 2024-01 starts on Monday
        Quarter q = quarter(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        assertEquals(DayOfWeek.MONDAY, LocalDate.of(2024, 1, 1).getDayOfWeek());

        List<CalendarMonth> months = CalendarViewModel.buildMonths(q, Collections.emptyList(), Collections.emptySet());
        CalendarDay first = months.get(0).days.get(0);
        assertNotNull("No leading null expected for Monday-start month", first);
        assertEquals(LocalDate.of(2024, 1, 1), first.date);
    }

    @Test
    public void buildMonths_monthStartingWednesday_hasTwoLeadingNulls() {
        // 2025-01 starts on Wednesday
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        assertEquals(DayOfWeek.WEDNESDAY, LocalDate.of(2025, 1, 1).getDayOfWeek());

        List<CalendarMonth> months = CalendarViewModel.buildMonths(q, Collections.emptyList(), Collections.emptySet());
        List<CalendarDay> cells = months.get(0).days;
        assertNull("Cell 0 should be Mon padding", cells.get(0));
        assertNull("Cell 1 should be Tue padding", cells.get(1));
        assertNotNull("Cell 2 should be Jan 1", cells.get(2));
        assertEquals(LocalDate.of(2025, 1, 1), cells.get(2).date);
    }

    @Test
    public void buildMonths_correctDayCountForJanuary() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        List<CalendarMonth> months = CalendarViewModel.buildMonths(q, Collections.emptyList(), Collections.emptySet());

        long nonNullCount = months.get(0).days.stream().filter(d -> d != null).count();
        assertEquals(31, nonNullCount);
    }

    // ── buildMonths — status mapping ─────────────────────────────────────────

    @Test
    public void buildMonths_attendedDay_hasInOfficeStatus() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        AttendanceDay ad = attendanceDay(LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE, false);

        List<CalendarMonth> months = CalendarViewModel.buildMonths(q, List.of(ad), Collections.emptySet());
        CalendarDay day = findDay(months, LocalDate.of(2025, 1, 6));

        assertNotNull(day);
        assertEquals(DayStatus.IN_OFFICE, day.status);
        assertFalse(day.isManualOverride);
    }

    @Test
    public void buildMonths_manualOverride_flaggedCorrectly() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        AttendanceDay ad = attendanceDay(LocalDate.of(2025, 1, 7), DayStatus.SICK, true);

        List<CalendarMonth> months = CalendarViewModel.buildMonths(q, List.of(ad), Collections.emptySet());
        CalendarDay day = findDay(months, LocalDate.of(2025, 1, 7));

        assertNotNull(day);
        assertEquals(DayStatus.SICK, day.status);
        assertTrue(day.isManualOverride);
    }

    @Test
    public void buildMonths_dayWithNoRecord_hasNullStatus() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        List<CalendarMonth> months = CalendarViewModel.buildMonths(q, Collections.emptyList(), Collections.emptySet());
        CalendarDay day = findDay(months, LocalDate.of(2025, 1, 8));

        assertNotNull(day);
        assertNull(day.status);
        assertFalse(day.isManualOverride);
    }

    // ── buildMonths — day flags ───────────────────────────────────────────────

    @Test
    public void buildMonths_saturday_isWeekend() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        List<CalendarMonth> months = CalendarViewModel.buildMonths(q, Collections.emptyList(), Collections.emptySet());

        // 2025-01-04 is a Saturday
        assertEquals(DayOfWeek.SATURDAY, LocalDate.of(2025, 1, 4).getDayOfWeek());
        CalendarDay day = findDay(months, LocalDate.of(2025, 1, 4));
        assertNotNull(day);
        assertTrue(day.isWeekend);
    }

    @Test
    public void buildMonths_friday_isNotWeekend() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        List<CalendarMonth> months = CalendarViewModel.buildMonths(q, Collections.emptyList(), Collections.emptySet());
        // 2025-01-03 is a Friday
        CalendarDay day = findDay(months, LocalDate.of(2025, 1, 3));
        assertNotNull(day);
        assertFalse(day.isWeekend);
    }

    @Test
    public void buildMonths_bankHoliday_isFlagged() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        Set<LocalDate> holidays = new HashSet<>();
        holidays.add(LocalDate.of(2025, 1, 1)); // New Year's

        List<CalendarMonth> months = CalendarViewModel.buildMonths(q, Collections.emptyList(), holidays);
        CalendarDay day = findDay(months, LocalDate.of(2025, 1, 1));

        assertNotNull(day);
        assertTrue(day.isBankHoliday);
    }

    @Test
    public void buildMonths_nonHoliday_isNotFlagged() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        List<CalendarMonth> months = CalendarViewModel.buildMonths(q, Collections.emptyList(), Collections.emptySet());
        CalendarDay day = findDay(months, LocalDate.of(2025, 1, 2));
        assertNotNull(day);
        assertFalse(day.isBankHoliday);
    }

    // ── updateDayStatusSync ───────────────────────────────────────────────────

    @Test
    public void updateDayStatusSync_existingDay_updatesStatusAndSetsManualOverride() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        long qId = quarterDao.insert(q);

        AttendanceDay ad = new AttendanceDay();
        ad.date = LocalDate.of(2025, 1, 6);
        ad.quarterId = qId;
        ad.status = DayStatus.NOT_IN_OFFICE;
        dayDao.insert(ad);

        vm.loadedQuarterId = qId;
        vm.updateDayStatusSync(LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE);

        AttendanceDay updated = dayDao.getByDate("2025-01-06");
        assertNotNull(updated);
        assertEquals(DayStatus.IN_OFFICE, updated.status);
        assertTrue(updated.isManualOverride);
    }

    @Test
    public void updateDayStatusSync_newDay_insertsWithManualOverride() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        long qId = quarterDao.insert(q);
        vm.loadedQuarterId = qId;

        vm.updateDayStatusSync(LocalDate.of(2025, 1, 8), DayStatus.SICK);

        AttendanceDay inserted = dayDao.getByDate("2025-01-08");
        assertNotNull(inserted);
        assertEquals(DayStatus.SICK, inserted.status);
        assertTrue(inserted.isManualOverride);
    }

    // ── bulk mode — state ─────────────────────────────────────────────────────

    @Test
    public void enterBulkMode_setsBulkModeTrue() {
        vm.enterBulkMode();
        assertTrue(vm.bulkMode.getValue());
    }

    @Test
    public void exitBulkMode_setsBulkModeFalse() {
        vm.enterBulkMode();
        vm.exitBulkMode();
        assertFalse(vm.bulkMode.getValue());
    }

    @Test
    public void exitBulkMode_clearsSelectedDays() {
        vm.enterBulkMode();
        vm.toggleBulkDay(LocalDate.of(2025, 1, 6));
        vm.exitBulkMode();
        assertTrue(vm.bulkSelectedDays.getValue().isEmpty());
    }

    @Test
    public void toggleBulkDay_addsDay() {
        LocalDate day = LocalDate.of(2025, 1, 6);
        vm.toggleBulkDay(day);
        assertTrue(vm.bulkSelectedDays.getValue().contains(day));
    }

    @Test
    public void toggleBulkDay_removesIfAlreadySelected() {
        LocalDate day = LocalDate.of(2025, 1, 6);
        vm.toggleBulkDay(day);
        vm.toggleBulkDay(day);
        assertFalse(vm.bulkSelectedDays.getValue().contains(day));
    }

    @Test
    public void setBulkStatus_updatesLiveData() {
        vm.setBulkStatus(DayStatus.SICK);
        assertEquals(DayStatus.SICK, vm.bulkSelectedStatus.getValue());
    }

    // ── bulk mode — applyBulkStatusSync ──────────────────────────────────────

    @Test
    public void applyBulkStatusSync_writesStatusForEachSelectedDay() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        long qId = quarterDao.insert(q);
        vm.loadedQuarterId = qId;

        vm.setBulkStatus(DayStatus.SICK);
        vm.toggleBulkDay(LocalDate.of(2025, 1, 6));
        vm.toggleBulkDay(LocalDate.of(2025, 1, 7));
        vm.applyBulkStatusSync();

        AttendanceDay d1 = dayDao.getByDate("2025-01-06");
        AttendanceDay d2 = dayDao.getByDate("2025-01-07");
        assertNotNull(d1);
        assertNotNull(d2);
        assertEquals(DayStatus.SICK, d1.status);
        assertEquals(DayStatus.SICK, d2.status);
        assertTrue(d1.isManualOverride);
        assertTrue(d2.isManualOverride);
    }

    @Test
    public void applyBulkStatusSync_exitsBulkModeAndClearsSelection() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        long qId = quarterDao.insert(q);
        vm.loadedQuarterId = qId;

        vm.enterBulkMode();
        vm.setBulkStatus(DayStatus.HOLIDAY);
        vm.toggleBulkDay(LocalDate.of(2025, 1, 8));
        vm.applyBulkStatusSync();

        assertFalse(vm.bulkMode.getValue());
        assertTrue(vm.bulkSelectedDays.getValue().isEmpty());
    }

    @Test
    public void clearBulkSelection_removesAllSelectedDaysWithoutExitingBulkMode() {
        vm.enterBulkMode();
        vm.toggleBulkDay(LocalDate.of(2025, 1, 6));
        vm.toggleBulkDay(LocalDate.of(2025, 1, 7));
        vm.clearBulkSelection();
        assertTrue(vm.bulkSelectedDays.getValue().isEmpty());
        assertTrue(vm.bulkMode.getValue());
    }

    @Test
    public void clearDayStatusSync_deletesExistingRecord() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        long qId = quarterDao.insert(q);
        vm.loadedQuarterId = qId;

        AttendanceDay ad = new AttendanceDay();
        ad.date = LocalDate.of(2025, 1, 6);
        ad.quarterId = qId;
        ad.status = DayStatus.IN_OFFICE;
        ad.isManualOverride = false;
        dayDao.insert(ad);

        vm.clearDayStatusSync(LocalDate.of(2025, 1, 6));

        assertNull(dayDao.getByDate("2025-01-06"));
    }

    @Test
    public void clearDayStatusSync_nonExistentDay_doesNotCrash() {
        vm.loadedQuarterId = 1;
        vm.clearDayStatusSync(LocalDate.of(2025, 1, 6)); // no record — must not throw
    }

    @Test
    public void applyBulkStatusSync_withNullStatus_deletesSelectedDays() {
        Quarter q = quarter(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        long qId = quarterDao.insert(q);
        vm.loadedQuarterId = qId;

        AttendanceDay ad = new AttendanceDay();
        ad.date = LocalDate.of(2025, 1, 6);
        ad.quarterId = qId;
        ad.status = DayStatus.SICK;
        ad.isManualOverride = true;
        dayDao.insert(ad);

        vm.setBulkStatus(null);
        vm.toggleBulkDay(LocalDate.of(2025, 1, 6));
        vm.applyBulkStatusSync();

        assertNull(dayDao.getByDate("2025-01-06"));
        assertFalse(vm.bulkMode.getValue());
        assertTrue(vm.bulkSelectedDays.getValue().isEmpty());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static Quarter quarter(LocalDate start, LocalDate end) {
        Quarter q = new Quarter();
        q.quarterNumber = 1;
        q.startDate = start;
        q.endDate = end;
        return q;
    }

    private static AttendanceDay attendanceDay(LocalDate date, DayStatus status, boolean manual) {
        AttendanceDay d = new AttendanceDay();
        d.date = date;
        d.quarterId = 1;
        d.status = status;
        d.isManualOverride = manual;
        return d;
    }

    private static CalendarDay findDay(List<CalendarMonth> months, LocalDate target) {
        for (CalendarMonth m : months) {
            for (CalendarDay d : m.days) {
                if (d != null && d.date.equals(target)) return d;
            }
        }
        return null;
    }
}
