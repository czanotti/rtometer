package com.rtometer.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rtometer.calculator.QuarterCalculator;
import com.rtometer.calculator.QuarterStats;
import com.rtometer.data.db.AttendanceDay;
import com.rtometer.data.db.AttendanceDayDao;
import com.rtometer.data.db.BankHolidayDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.QuarterDao;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DashboardRepository {

    private final LiveData<Quarter> currentQuarter;
    private final LiveData<QuarterStats> stats;

    @Inject
    public DashboardRepository(QuarterDao quarterDao,
                                AttendanceDayDao attendanceDayDao,
                                BankHolidayDao bankHolidayDao) {
        String today = LocalDate.now().toString();
        currentQuarter = quarterDao.observeByDate(today);

        LiveData<List<AttendanceDay>> days = Transformations.switchMap(currentQuarter, q -> {
            if (q == null) return new MutableLiveData<>(Collections.emptyList());
            return attendanceDayDao.observeByQuarterId(q.id);
        });

        LiveData<List<LocalDate>> holidays = Transformations.switchMap(currentQuarter, q -> {
            if (q == null) return new MutableLiveData<>(Collections.emptyList());
            return bankHolidayDao.observeDatesForYear(q.startDate.getYear());
        });

        MediatorLiveData<QuarterStats> mediator = new MediatorLiveData<>();
        Quarter[] latestQuarter = {null};
        List<AttendanceDay>[] latestDays = new List[]{Collections.emptyList()};
        List<LocalDate>[] latestHolidays = new List[]{Collections.emptyList()};

        mediator.addSource(currentQuarter, q -> {
            latestQuarter[0] = q;
            recompute(mediator, latestQuarter[0], latestDays[0], latestHolidays[0]);
        });
        mediator.addSource(days, d -> {
            if (d != null) latestDays[0] = d;
            recompute(mediator, latestQuarter[0], latestDays[0], latestHolidays[0]);
        });
        mediator.addSource(holidays, h -> {
            if (h != null) latestHolidays[0] = h;
            recompute(mediator, latestQuarter[0], latestDays[0], latestHolidays[0]);
        });

        stats = mediator;
    }

    public LiveData<Quarter> getCurrentQuarter() {
        return currentQuarter;
    }

    public LiveData<QuarterStats> getStats() {
        return stats;
    }

    private static void recompute(MediatorLiveData<QuarterStats> target,
                                   Quarter quarter,
                                   List<AttendanceDay> days,
                                   List<LocalDate> holidays) {
        if (quarter == null) {
            target.setValue(null);
            return;
        }
        target.setValue(QuarterCalculator.calculate(quarter, days, holidays, LocalDate.now()));
    }
}
