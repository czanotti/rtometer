package com.rtometer.ui.onboarding;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rtometer.calculator.FiscalQuarterPreset;
import com.rtometer.data.db.AppConfig;
import com.rtometer.data.db.AppConfigDao;
import com.rtometer.data.db.AppDatabase;
import com.rtometer.data.db.BankHolidayDao;
import com.rtometer.data.db.Office;
import com.rtometer.data.db.OfficeDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.QuarterDao;

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
        vm = new OnboardingViewModel(quarterDao, officeDao, configDao, db.bankHolidayDao());
        vm.setOfficeName("Kings Building");
        vm.setOfficeLat(53.3478);
        vm.setOfficeLng(-6.27591);
        vm.setOfficeRadiusMeters(200);
    }

    @After
    public void tearDown() {
        vm.onCleared();
        db.close();
    }

    @Test
    public void persist_febStartPreset_createsFourQuarters() {
        vm.setPreset(FiscalQuarterPreset.FEB_START);
        vm.setTargetPercentage(50);
        vm.persistSync();

        List<Quarter> quarters = quarterDao.getAll();
        assertEquals(4, quarters.size());
        Quarter q1 = findByNumber(quarters, 1);
        assertNotNull(q1);
        assertEquals(2, q1.startDate.getMonthValue());
        assertEquals(0.5f, q1.targetPercentage, 0.001f);
    }

    @Test
    public void persist_calendarPreset_q1StartsInJanuary() {
        vm.setPreset(FiscalQuarterPreset.CALENDAR);
        vm.persistSync();

        List<Quarter> quarters = quarterDao.getAll();
        Quarter q1 = findByNumber(quarters, 1);
        assertNotNull(q1);
        assertEquals(1, q1.startDate.getMonthValue());
    }

    @Test
    public void persist_customPresetMonth3_q1StartsInMarch() {
        vm.setPreset(FiscalQuarterPreset.CUSTOM);
        vm.setCustomStartMonth(3);
        vm.persistSync();

        List<Quarter> quarters = quarterDao.getAll();
        assertEquals(4, quarters.size());
        Quarter q1 = findByNumber(quarters, 1);
        assertNotNull(q1);
        assertEquals(3, q1.startDate.getMonthValue());
    }

    @Test
    public void persist_insertsOfficePrimary() {
        vm.setPreset(FiscalQuarterPreset.FEB_START);
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
    public void persist_storesWorkHoursAndGpsInterval() {
        vm.setPreset(FiscalQuarterPreset.FEB_START);
        vm.setWorkDayStart(LocalTime.of(9, 30));
        vm.setWorkDayEnd(LocalTime.of(18, 0));
        vm.setGpsIntervalMinutes(120);
        vm.persistSync();

        AppConfig cfg = configDao.get();
        assertNotNull(cfg);
        assertEquals(LocalTime.of(9, 30), cfg.workDayStart);
        assertEquals(LocalTime.of(18, 0), cfg.workDayEnd);
        assertEquals(120, cfg.gpsIntervalMinutes);
    }

    @Test
    public void persist_defaultsFebStartAndCorrectWorkTimes() {
        vm.persistSync();

        AppConfig cfg = configDao.get();
        assertNotNull(cfg);
        assertEquals("FEB_START", cfg.fiscalQuarterPreset);
        assertEquals(LocalTime.of(9, 30), cfg.workDayStart);
        assertEquals(120, cfg.gpsIntervalMinutes);
    }

    private Quarter findByNumber(List<Quarter> list, int n) {
        for (Quarter q : list) {
            if (q.quarterNumber == n) return q;
        }
        return null;
    }
}
