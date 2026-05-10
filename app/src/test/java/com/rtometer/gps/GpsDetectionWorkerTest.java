package com.rtometer.gps;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.work.Configuration;
import androidx.work.ListenableWorker;
import androidx.work.testing.TestWorkerBuilder;
import androidx.work.testing.WorkManagerTestInitHelper;

import com.rtometer.data.db.AppDatabase;
import com.rtometer.data.db.AttendanceDay;
import com.rtometer.data.db.AttendanceDayDao;
import com.rtometer.data.db.Office;
import com.rtometer.data.db.OfficeDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.QuarterDao;
import com.rtometer.data.model.DayStatus;
import com.rtometer.gps.LocationPermissionChecker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalDate;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class GpsDetectionWorkerTest {

    // WorkManager is a singleton — initialize it once per class to avoid
    // abandoning multiple WorkDatabase instances that leak via CloseGuard.
    private static boolean wmInitialized = false;

    private AppDatabase db;
    private AttendanceDayDao dayDao;
    private OfficeDao officeDao;
    private QuarterDao quarterDao;
    private Context context;

    // London HQ at (51.5074, -0.1278), 200 m radius
    private static final double HQ_LAT = 51.5074;
    private static final double HQ_LNG = -0.1278;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        AppDatabase.setTestInstance(db);
        dayDao = db.attendanceDayDao();
        officeDao = db.officeDao();
        quarterDao = db.quarterDao();

        if (!wmInitialized) {
            Configuration config = new Configuration.Builder()
                    .setMinimumLoggingLevel(Log.DEBUG)
                    .build();
            WorkManagerTestInitHelper.initializeTestWorkManager(context, config);
            wmInitialized = true;
        }
    }

    @After
    public void teardown() {
        GpsDetectionWorker.testLatLng = null;
        AppDatabase.setTestInstance(null);
        db.close();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private long insertOffice(double lat, double lng, int radius) {
        Office o = new Office();
        o.name = "HQ";
        o.latitude = lat;
        o.longitude = lng;
        o.radiusMeters = radius;
        return officeDao.insert(o);
    }

    private long insertQuarterContainingToday() {
        Quarter q = new Quarter();
        q.fiscalYear = 2025;
        q.quarterNumber = 1;
        q.startDate = LocalDate.now().minusMonths(1);
        q.endDate = LocalDate.now().plusMonths(2);
        return quarterDao.insert(q);
    }

    private long insertDayForToday(long quarterId, DayStatus status) {
        AttendanceDay d = new AttendanceDay();
        d.date = LocalDate.now();
        d.quarterId = quarterId;
        d.status = status;
        return dayDao.insert(d);
    }

    private GpsDetectionWorker buildWorker() {
        return TestWorkerBuilder.from(context, GpsDetectionWorker.class,
                Executors.newSingleThreadExecutor()).build();
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    public void withinOfficeRadius_marksInOffice() {
        insertOffice(HQ_LAT, HQ_LNG, 200);
        long quarterId = insertQuarterContainingToday();
        insertDayForToday(quarterId, DayStatus.NOT_IN_OFFICE);

        GpsDetectionWorker.testLatLng = new double[]{HQ_LAT, HQ_LNG}; // same coords → distance = 0

        ListenableWorker.Result result = buildWorker().doWork();

        assertEquals(ListenableWorker.Result.success(), result);
        AttendanceDay updated = dayDao.getByDate(LocalDate.now().toString());
        assertNotNull(updated);
        assertEquals(DayStatus.IN_OFFICE, updated.status);
    }

    @Test
    public void outsideAllOffices_noStatusChange() {
        insertOffice(HQ_LAT, HQ_LNG, 200);
        long quarterId = insertQuarterContainingToday();
        insertDayForToday(quarterId, DayStatus.NOT_IN_OFFICE);

        // ~1 km north of HQ
        GpsDetectionWorker.testLatLng = new double[]{51.5164, HQ_LNG};

        buildWorker().doWork();

        AttendanceDay result = dayDao.getByDate(LocalDate.now().toString());
        assertNotNull(result);
        assertEquals(DayStatus.NOT_IN_OFFICE, result.status);
    }

    @Test
    public void alreadyInOffice_exitEarly_returnsSuccess() {
        long quarterId = insertQuarterContainingToday();
        insertDayForToday(quarterId, DayStatus.IN_OFFICE);

        // testLatLng not set — would NPE if we reached GPS logic
        GpsDetectionWorker.testLatLng = null;

        ListenableWorker.Result result = buildWorker().doWork();
        assertEquals(ListenableWorker.Result.success(), result);
    }

    @Test
    public void noOfficesRegistered_noStatusChange() {
        long quarterId = insertQuarterContainingToday();
        insertDayForToday(quarterId, DayStatus.NOT_IN_OFFICE);

        GpsDetectionWorker.testLatLng = new double[]{HQ_LAT, HQ_LNG};

        buildWorker().doWork();

        AttendanceDay result = dayDao.getByDate(LocalDate.now().toString());
        assertEquals(DayStatus.NOT_IN_OFFICE, result.status);
    }

    @Test
    public void noDayRecord_withinRadius_insertsInOffice() {
        insertOffice(HQ_LAT, HQ_LNG, 200);
        insertQuarterContainingToday();
        // No AttendanceDay row for today

        GpsDetectionWorker.testLatLng = new double[]{HQ_LAT, HQ_LNG};

        buildWorker().doWork();

        AttendanceDay result = dayDao.getByDate(LocalDate.now().toString());
        assertNotNull(result);
        assertEquals(DayStatus.IN_OFFICE, result.status);
    }

    @Test
    public void permissionDenied_setsDeniedFlagAndLeavesDayUnchanged() {
        insertOffice(HQ_LAT, HQ_LNG, 200);
        long quarterId = insertQuarterContainingToday();
        insertDayForToday(quarterId, DayStatus.NOT_IN_OFFICE);

        // testLatLng = null → falls through to real permission check;
        // Robolectric grants no permissions by default so hasBackgroundLocation() = false
        GpsDetectionWorker.testLatLng = null;

        ListenableWorker.Result result = buildWorker().doWork();

        assertEquals(ListenableWorker.Result.success(), result);
        assertTrue(LocationPermissionChecker.isDenied(context));
        AttendanceDay day = dayDao.getByDate(LocalDate.now().toString());
        assertNotNull(day);
        assertEquals(DayStatus.NOT_IN_OFFICE, day.status);
    }
}
