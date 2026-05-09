package com.rtometer.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.rtometer.data.db.AppDatabase;
import com.rtometer.data.db.AttendanceDay;
import com.rtometer.data.db.AttendanceDayDao;
import com.rtometer.data.db.Office;
import com.rtometer.data.db.OfficeDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.QuarterDao;
import com.rtometer.data.model.DayStatus;

import java.time.LocalDate;
import java.util.List;

public class GpsDetectionWorker extends Worker {

    /** Set in tests to bypass real GPS without location permission. */
    @VisibleForTesting
    static volatile double[] testLatLng = null;

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

        String today = LocalDate.now().toString();

        // Exit early if day is already confirmed
        AttendanceDay existing = dayDao.getByDate(today);
        if (existing != null && existing.status == DayStatus.IN_OFFICE) {
            return Result.success();
        }

        double[] latLng = resolveLatLng(context);
        if (latLng == null) {
            return Result.success();
        }

        List<Office> offices = officeDao.getAll();
        for (Office office : offices) {
            if (GeofenceChecker.isWithin(latLng[0], latLng[1], office)) {
                markInOffice(dayDao, quarterDao, existing, today, office.id);
                GpsScheduler.cancelToday(context);
                return Result.success();
            }
        }

        return Result.success();
    }

    private double[] resolveLatLng(Context context) {
        if (testLatLng != null) {
            return testLatLng;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        // Real GPS via FusedLocationProviderClient is wired up in RTO-14.
        return null;
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
