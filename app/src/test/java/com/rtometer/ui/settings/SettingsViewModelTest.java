package com.rtometer.ui.settings;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rtometer.calculator.FiscalQuarterPreset;
import com.rtometer.data.db.AppConfig;
import com.rtometer.data.db.AppConfigDao;
import com.rtometer.data.db.AppDatabase;
import com.rtometer.data.db.AttendanceDay;
import com.rtometer.data.db.AttendanceDayDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.BankHolidayDao;
import com.rtometer.data.db.QuarterDao;
import com.rtometer.data.model.DayStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SettingsViewModelTest {

    private AppDatabase db;
    private AppConfigDao configDao;
    private QuarterDao quarterDao;
    private AttendanceDayDao attendanceDayDao;
    private SettingsViewModel vm;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        configDao = db.appConfigDao();
        quarterDao = db.quarterDao();
        attendanceDayDao = db.attendanceDayDao();
        vm = new SettingsViewModel(configDao, quarterDao, db.bankHolidayDao());
    }

    @After
    public void tearDown() {
        vm.onCleared();
        db.close();
    }

    @Test
    public void saveConfigSync_persistsWorkHoursAndGpsInterval() {
        vm.saveConfigSync(LocalTime.of(9, 0), LocalTime.of(17, 30), 30,
                null, FiscalQuarterPreset.CALENDAR, 1);

        AppConfig cfg = configDao.get();
        assertNotNull(cfg);
        assertEquals(LocalTime.of(9, 0), cfg.workDayStart);
        assertEquals(LocalTime.of(17, 30), cfg.workDayEnd);
        assertEquals(30, cfg.gpsIntervalMinutes);
    }

    @Test
    public void saveConfigSync_persistsBankHolidayCountry() {
        vm.saveConfigSync(LocalTime.of(8, 0), LocalTime.of(18, 0), 15,
                "IT", FiscalQuarterPreset.CALENDAR, 1);

        AppConfig cfg = configDao.get();
        assertNotNull(cfg);
        assertEquals("IT", cfg.bankHolidayCountry);
    }

    @Test
    public void saveConfigSync_persistsPreset() {
        vm.saveConfigSync(LocalTime.of(8, 0), LocalTime.of(18, 0), 15,
                null, FiscalQuarterPreset.APR_START, 1);

        AppConfig cfg = configDao.get();
        assertNotNull(cfg);
        assertEquals("APR_START", cfg.fiscalQuarterPreset);
    }

    @Test
    public void saveConfigSync_customPreset_persistsCustomStartMonth() {
        vm.saveConfigSync(LocalTime.of(8, 0), LocalTime.of(18, 0), 15,
                null, FiscalQuarterPreset.CUSTOM, 3);

        AppConfig cfg = configDao.get();
        assertNotNull(cfg);
        assertEquals("CUSTOM", cfg.fiscalQuarterPreset);
        assertEquals(3, cfg.customStartMonth);
    }

    @Test
    public void saveQuarterTargetSync_updatesTarget() {
        Quarter q = quarter(2026, 1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 0.5f);
        long id = quarterDao.insert(q);

        vm.saveQuarterTargetSync(id, 0.8f);

        Quarter updated = quarterDao.getById(id);
        assertEquals(0.8f, updated.targetPercentage, 0.001f);
    }

    @Test
    public void saveQuarterTargetSync_nonExistentId_doesNotCrash() {
        vm.saveQuarterTargetSync(999L, 0.7f);
    }

    @Test
    public void resetQuartersSync_deletesPreviousAndCreatesFour() {
        quarterDao.insert(quarter(2025, 1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31), 0.6f));

        vm.resetQuartersSync(FiscalQuarterPreset.CALENDAR, 1, 2026, 0.6f);

        List<Quarter> quarters = quarterDao.getAll();
        assertEquals(4, quarters.size());
        assertTrue(quarters.stream().allMatch(qx -> qx.fiscalYear == 2026));
    }

    @Test
    public void resetQuartersSync_cascadeDeletesAttendanceDays() {
        Quarter q = quarter(2025, 1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31), 0.6f);
        long qId = quarterDao.insert(q);

        AttendanceDay day = new AttendanceDay();
        day.quarterId = qId;
        day.date = LocalDate.of(2025, 1, 5);
        day.status = DayStatus.IN_OFFICE;
        attendanceDayDao.insert(day);

        vm.resetQuartersSync(FiscalQuarterPreset.CALENDAR, 1, 2026, 0.6f);

        assertEquals(0, attendanceDayDao.getByQuarterId(qId).size());
    }

    @Test
    public void resetQuartersSync_appliesTargetToAllNewQuarters() {
        vm.resetQuartersSync(FiscalQuarterPreset.CALENDAR, 1, 2026, 0.75f);

        List<Quarter> quarters = quarterDao.getAll();
        assertTrue(quarters.stream().allMatch(qx -> Math.abs(qx.targetPercentage - 0.75f) < 0.001f));
    }

    @Test
    public void resetQuartersSync_storesPresetInConfig() {
        AppConfig existing = new AppConfig();
        existing.id = 1;
        configDao.upsert(existing);

        vm.resetQuartersSync(FiscalQuarterPreset.MAY_START, 1, 2026, 0.6f);

        AppConfig cfg = configDao.get();
        assertNotNull(cfg);
        assertEquals("MAY_START", cfg.fiscalQuarterPreset);
    }

    @Test
    public void resetQuartersSync_customPreset_createsQuartersFromCustomMonth() {
        vm.resetQuartersSync(FiscalQuarterPreset.CUSTOM, 3, 2026, 0.6f);

        List<Quarter> quarters = quarterDao.getAll();
        assertEquals(4, quarters.size());
        Quarter q1 = quarters.stream().filter(qx -> qx.quarterNumber == 1).findFirst().orElse(null);
        assertNotNull(q1);
        assertEquals(LocalDate.of(2026, 3, 1), q1.startDate);
    }

    private Quarter quarter(int fy, int qn, LocalDate start, LocalDate end, float target) {
        Quarter q = new Quarter();
        q.fiscalYear = fy;
        q.quarterNumber = qn;
        q.startDate = start;
        q.endDate = end;
        q.targetPercentage = target;
        return q;
    }
}
