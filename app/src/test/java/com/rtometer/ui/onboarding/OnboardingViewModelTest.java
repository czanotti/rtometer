package com.rtometer.ui.onboarding;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rtometer.calculator.FiscalQuarterPreset;
import com.rtometer.data.db.AppConfig;
import com.rtometer.data.db.AppConfigDao;
import com.rtometer.data.db.AppDatabase;
import com.rtometer.data.db.Office;
import com.rtometer.data.db.OfficeDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.QuarterDao;
import com.rtometer.data.model.BankHolidayCountry;

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
public class OnboardingViewModelTest {

    private AppDatabase db;
    private QuarterDao quarterDao;
    private OfficeDao officeDao;
    private AppConfigDao configDao;
    private OnboardingViewModel vm;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        quarterDao = db.quarterDao();
        officeDao = db.officeDao();
        configDao = db.appConfigDao();
        vm = new OnboardingViewModel(quarterDao, officeDao, configDao);
        vm.setOfficeName("HQ");
        vm.setOfficeLat(51.5);
        vm.setOfficeLng(-0.1);
        vm.setOfficeRadiusMeters(200);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void persist_calendarPreset2026_createsFourQuarters() {
        vm.setPreset(FiscalQuarterPreset.CALENDAR);
        vm.setSelectedYear(2026);
        vm.setTargetPercentage(60);
        vm.persistSync();

        List<Quarter> quarters = quarterDao.getAll();
        assertEquals(4, quarters.size());
        Quarter q1 = findByNumber(quarters, 1);
        assertNotNull(q1);
        assertEquals(LocalDate.of(2026, 1, 1), q1.startDate);
        assertEquals(LocalDate.of(2026, 3, 31), q1.endDate);
        assertEquals(0.6f, q1.targetPercentage, 0.001f);
    }

    @Test
    public void persist_customPresetMonth3_q1StartsInMarch() {
        vm.setPreset(FiscalQuarterPreset.CUSTOM);
        vm.setCustomStartMonth(3);
        vm.setSelectedYear(2026);
        vm.persistSync();

        List<Quarter> quarters = quarterDao.getAll();
        assertEquals(4, quarters.size());
        Quarter q1 = findByNumber(quarters, 1);
        assertNotNull(q1);
        assertEquals(LocalDate.of(2026, 3, 1), q1.startDate);
    }

    @Test
    public void persist_withPreloadCount_exactlyOneQuarterHasIt() {
        vm.setPreset(FiscalQuarterPreset.CALENDAR);
        vm.setSelectedYear(LocalDate.now().getYear());
        vm.setPreloadCount(5);
        vm.persistSync();

        List<Quarter> quarters = quarterDao.getAll();
        long preloaded = quarters.stream().filter(q -> q.preloadCount == 5).count();
        assertEquals(1, preloaded);
    }

    @Test
    public void persist_zeroPreloadCount_noQuarterHasPreload() {
        vm.setPreset(FiscalQuarterPreset.CALENDAR);
        vm.setSelectedYear(2026);
        vm.setPreloadCount(0);
        vm.persistSync();

        List<Quarter> quarters = quarterDao.getAll();
        assertTrue(quarters.stream().allMatch(q -> q.preloadCount == 0));
    }

    @Test
    public void persist_insertsOfficePrimary() {
        vm.setPreset(FiscalQuarterPreset.CALENDAR);
        vm.setSelectedYear(2026);
        vm.setOfficeName("Paris HQ");
        vm.setOfficeLat(48.8566);
        vm.setOfficeLng(2.3522);
        vm.setOfficeRadiusMeters(300);
        vm.persistSync();

        Office o = officeDao.getPrimary();
        assertNotNull(o);
        assertTrue(o.isPrimary);
        assertEquals("Paris HQ", o.name);
        assertEquals(48.8566, o.latitude, 0.0001);
        assertEquals(2.3522, o.longitude, 0.0001);
        assertEquals(300, o.radiusMeters);
    }

    @Test
    public void persist_withBankHolidayIT_storesInConfig() {
        vm.setPreset(FiscalQuarterPreset.CALENDAR);
        vm.setSelectedYear(2026);
        vm.setBankHolidayCountry(BankHolidayCountry.IT);
        vm.persistSync();

        AppConfig cfg = configDao.get();
        assertNotNull(cfg);
        assertEquals("IT", cfg.bankHolidayCountry);
    }

    @Test
    public void persist_noBankHolidayCountry_storesNull() {
        vm.setPreset(FiscalQuarterPreset.CALENDAR);
        vm.setSelectedYear(2026);
        vm.setBankHolidayCountry(null);
        vm.persistSync();

        AppConfig cfg = configDao.get();
        assertNotNull(cfg);
        assertNull(cfg.bankHolidayCountry);
    }

    @Test
    public void persist_storesWorkHoursAndGpsInterval() {
        vm.setPreset(FiscalQuarterPreset.CALENDAR);
        vm.setSelectedYear(2026);
        vm.setWorkDayStart(LocalTime.of(9, 0));
        vm.setWorkDayEnd(LocalTime.of(17, 30));
        vm.setGpsIntervalMinutes(30);
        vm.persistSync();

        AppConfig cfg = configDao.get();
        assertNotNull(cfg);
        assertEquals(LocalTime.of(9, 0), cfg.workDayStart);
        assertEquals(LocalTime.of(17, 30), cfg.workDayEnd);
        assertEquals(30, cfg.gpsIntervalMinutes);
    }

    private Quarter findByNumber(List<Quarter> list, int n) {
        for (Quarter q : list) {
            if (q.quarterNumber == n) return q;
        }
        return null;
    }
}
