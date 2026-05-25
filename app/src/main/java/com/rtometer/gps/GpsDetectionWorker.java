package com.rtometer.gps;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.rtometer.data.db.AppConfig;
import com.rtometer.data.db.AppDatabase;
import com.rtometer.data.db.AttendanceDay;
import com.rtometer.data.db.AttendanceDayDao;
import com.rtometer.data.db.Office;
import com.rtometer.data.db.OfficeDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.QuarterDao;
import com.rtometer.data.model.DayStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class GpsDetectionWorker extends Worker {

    /** Set in tests to bypass real GPS without location permission. */
    @VisibleForTesting
    static volatile double[] testLatLng = null;

    /** Set in tests to override current time for working-hours checks. */
    @VisibleForTesting
    static volatile LocalTime testNow = null;

    /** Set in tests to override today's date (e.g. to simulate weekends). */
    @VisibleForTesting
    static volatile LocalDate testToday = null;

    public GpsDetectionWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        AttendanceDayDao dayDao = db.attendanceDayDao();
        OfficeDao officeDao = db.officeDao();
        QuarterDao quarterDao = db.quarterDao();

        LocalDate today = testToday != null ? testToday : LocalDate.now();
        String todayStr = today.toString();

        AppConfig config = db.appConfigDao().get();
        int intervalMinutes = config != null ? config.gpsIntervalMinutes : 120;
        long nextFixMs = System.currentTimeMillis() + intervalMinutes * 60_000L;
        DebugPrefs.saveNextFixMs(context, nextFixMs);

        DayOfWeek dow = today.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            DebugPrefs.saveRejection(context, "skipped: weekend (" + dow.name().toLowerCase() + ")");
            return Result.success();
        }

        AttendanceDay existing = dayDao.getByDate(todayStr);
        if (existing != null && existing.status != DayStatus.CLEAR) {
            DebugPrefs.saveRejection(context, "skipped: day is " + existing.status.name());
            return Result.success();
        }

        if (config != null) {
            LocalTime now = testNow != null ? testNow : LocalTime.now();
            if (now.isBefore(config.workDayStart) || now.isAfter(config.workDayEnd)) {
                DebugPrefs.saveRejection(context, "skipped: outside working hours (" + now + ")");
                return Result.success();
            }
        }

        double[] latLng = resolveLatLng(context);
        if (latLng == null) {
            return Result.success();
        }

        List<Office> offices = officeDao.getAll();
        double minDistance = Double.MAX_VALUE;
        long detectedOfficeId = -1;

        for (Office office : offices) {
            double dist = GeofenceChecker.distanceMeters(latLng[0], latLng[1],
                    office.latitude, office.longitude);
            if (dist < minDistance) {
                minDistance = dist;
            }
            if (detectedOfficeId < 0 && dist <= office.radiusMeters) {
                detectedOfficeId = office.id;
            }
        }

        DebugPrefs.pushFix(context, latLng[0], latLng[1],
                minDistance < Double.MAX_VALUE ? (float) minDistance : -1f,
                nextFixMs);

        if (detectedOfficeId >= 0) {
            markInOffice(dayDao, quarterDao, existing, todayStr, detectedOfficeId);
        }

        return Result.success();
    }

    private double[] resolveLatLng(Context context) {
        if (testLatLng != null) {
            return testLatLng;
        }
        if (!LocationPermissionChecker.hasBackgroundLocation(context)) {
            LocationPermissionChecker.setDenied(context, true);
            return null;
        }
        LocationPermissionChecker.setDenied(context, false);
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location gps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location network = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location loc = LocationUtils.pickBestLocation(gps, network);
            if (loc == null) {
                DebugPrefs.saveRejection(context, LocationUtils.describeRejection(gps, network));
                loc = LocationUtils.requestFreshLocation(lm);
            }
            if (loc == null) {
                return null;
            }
            return new double[]{loc.getLatitude(), loc.getLongitude()};
        } catch (SecurityException e) {
            return null;
        }
    }

    private void markInOffice(AttendanceDayDao dayDao, QuarterDao quarterDao,
                               AttendanceDay existing, String dateStr, long officeId) {
        if (existing != null) {
            existing.status = DayStatus.IN_OFFICE;
            existing.detectedOfficeId = officeId;
            dayDao.update(existing);
        } else {
            Quarter quarter = quarterDao.getByDate(dateStr);
            AttendanceDay day = new AttendanceDay();
            day.date = LocalDate.parse(dateStr);
            day.quarterId = quarter != null ? quarter.id : 0;
            day.status = DayStatus.IN_OFFICE;
            day.detectedOfficeId = officeId;
            dayDao.insert(day);
        }
    }
}
