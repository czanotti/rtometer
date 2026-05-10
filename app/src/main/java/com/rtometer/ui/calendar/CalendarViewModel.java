package com.rtometer.ui.calendar;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rtometer.data.db.AttendanceDay;
import com.rtometer.data.db.AttendanceDayDao;
import com.rtometer.data.db.BankHolidayDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.QuarterDao;
import com.rtometer.data.model.DayStatus;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CalendarViewModel extends ViewModel {

    final AttendanceDayDao dayDao;
    final QuarterDao quarterDao;
    final BankHolidayDao bankHolidayDao;

    private final MutableLiveData<List<CalendarMonth>> monthsLive = new MutableLiveData<>();
    public final LiveData<List<CalendarMonth>> months = monthsLive;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @VisibleForTesting
    long loadedQuarterId = -1;

    @Inject
    public CalendarViewModel(AttendanceDayDao dayDao, QuarterDao quarterDao,
                              BankHolidayDao bankHolidayDao) {
        this.dayDao = dayDao;
        this.quarterDao = quarterDao;
        this.bankHolidayDao = bankHolidayDao;
    }

    public void loadQuarter(long quarterId) {
        loadedQuarterId = quarterId;
        executor.execute(() -> refresh(quarterId));
    }

    private void refresh(long quarterId) {
        Quarter quarter = quarterDao.getById(quarterId);
        if (quarter == null) return;
        List<AttendanceDay> days = dayDao.getByQuarterId(quarterId);
        Set<LocalDate> holidays = collectBankHolidays(quarter);
        monthsLive.postValue(buildMonths(quarter, days, holidays));
    }

    public void updateDayStatus(LocalDate date, DayStatus newStatus) {
        long qId = loadedQuarterId;
        executor.execute(() -> {
            writeStatus(date, newStatus, qId);
            if (qId >= 0) refresh(qId);
        });
    }

    @Override
    protected void onCleared() {
        executor.shutdown();
        try { executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS); }
        catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    @VisibleForTesting
    void updateDayStatusSync(LocalDate date, DayStatus newStatus) {
        writeStatus(date, newStatus, loadedQuarterId);
    }

    private void writeStatus(LocalDate date, DayStatus newStatus, long fallbackQuarterId) {
        AttendanceDay existing = dayDao.getByDate(date.toString());
        if (existing != null) {
            existing.status = newStatus;
            existing.isManualOverride = true;
            dayDao.update(existing);
        } else {
            Quarter q = quarterDao.getByDate(date.toString());
            AttendanceDay d = new AttendanceDay();
            d.date = date;
            d.quarterId = q != null ? q.id : fallbackQuarterId;
            d.status = newStatus;
            d.isManualOverride = true;
            dayDao.insert(d);
        }
    }

    private Set<LocalDate> collectBankHolidays(Quarter quarter) {
        Set<LocalDate> result = new HashSet<>();
        int startYear = quarter.startDate.getYear();
        int endYear = quarter.endDate.getYear();
        for (int y = startYear; y <= endYear; y++) {
            result.addAll(bankHolidayDao.getDatesForYear(y));
        }
        return result;
    }

    @VisibleForTesting
    static List<CalendarMonth> buildMonths(Quarter quarter,
                                            List<AttendanceDay> attendanceDays,
                                            Set<LocalDate> bankHolidays) {
        Map<String, AttendanceDay> dayMap = new HashMap<>();
        for (AttendanceDay d : attendanceDays) {
            dayMap.put(d.date.toString(), d);
        }

        LocalDate today = LocalDate.now();
        List<CalendarMonth> result = new ArrayList<>();

        YearMonth startYM = YearMonth.from(quarter.startDate);
        YearMonth endYM = YearMonth.from(quarter.endDate);

        for (YearMonth ym = startYM; !ym.isAfter(endYM); ym = ym.plusMonths(1)) {
            List<CalendarDay> cells = new ArrayList<>();

            // Mon-origin padding: Mon=1 → 0 empties, Tue=2 → 1, ..., Sun=7 → 6
            int leadingEmpties = ym.atDay(1).getDayOfWeek().getValue() - 1;
            for (int i = 0; i < leadingEmpties; i++) {
                cells.add(null);
            }

            LocalDate rangeStart = ym.atDay(1).isBefore(quarter.startDate)
                    ? quarter.startDate : ym.atDay(1);
            LocalDate rangeEnd = ym.atEndOfMonth().isAfter(quarter.endDate)
                    ? quarter.endDate : ym.atEndOfMonth();

            for (LocalDate d = rangeStart; !d.isAfter(rangeEnd); d = d.plusDays(1)) {
                String ds = d.toString();
                AttendanceDay ad = dayMap.get(ds);
                DayStatus status = ad != null ? ad.status : null;
                boolean isManualOverride = ad != null && ad.isManualOverride;
                long id = ad != null ? ad.id : 0;
                boolean isWeekend = d.getDayOfWeek().getValue() >= 6;
                boolean isBankHoliday = bankHolidays.contains(d);
                boolean isToday = d.equals(today);

                cells.add(new CalendarDay(d, status, isManualOverride, isToday,
                        isWeekend, isBankHoliday, id));
            }

            result.add(new CalendarMonth(ym, cells));
        }

        return result;
    }
}
