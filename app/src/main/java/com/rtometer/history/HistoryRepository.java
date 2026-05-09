package com.rtometer.history;

import com.rtometer.calculator.QuarterCalculator;
import com.rtometer.calculator.QuarterStats;
import com.rtometer.data.db.AttendanceDay;
import com.rtometer.data.db.AttendanceDayDao;
import com.rtometer.data.db.BankHolidayDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.QuarterDao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HistoryRepository {

    private final QuarterDao quarterDao;
    private final AttendanceDayDao dayDao;
    private final BankHolidayDao bankHolidayDao;

    @Inject
    public HistoryRepository(QuarterDao quarterDao,
                              AttendanceDayDao dayDao,
                              BankHolidayDao bankHolidayDao) {
        this.quarterDao = quarterDao;
        this.dayDao = dayDao;
        this.bankHolidayDao = bankHolidayDao;
    }

    public List<PastQuarterEntry> loadPastQuarters() {
        LocalDate today = LocalDate.now();
        List<Quarter> all = quarterDao.getAll();
        List<PastQuarterEntry> result = new ArrayList<>();
        for (Quarter q : all) {
            if (!q.endDate.isBefore(today)) continue;
            List<AttendanceDay> days = dayDao.getByQuarterId(q.id);
            List<LocalDate> holidays = collectHolidays(q);
            QuarterStats stats = QuarterCalculator.calculate(q, days, holidays, today);
            result.add(new PastQuarterEntry(q, stats));
        }
        return result;
    }

    private List<LocalDate> collectHolidays(Quarter quarter) {
        List<LocalDate> result = new ArrayList<>();
        for (int y = quarter.startDate.getYear(); y <= quarter.endDate.getYear(); y++) {
            result.addAll(bankHolidayDao.getDatesForYear(y));
        }
        return result;
    }
}
