package com.rtometer.calculator;

import com.rtometer.data.db.AttendanceDay;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.model.DayStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuarterCalculator {

    public static QuarterStats calculate(
            Quarter quarter,
            List<AttendanceDay> days,
            List<LocalDate> bankHolidays,
            LocalDate today) {

        Set<LocalDate> bankHolidaySet = new HashSet<>(bankHolidays);
        Map<LocalDate, DayStatus> statusByDate = new HashMap<>();
        for (AttendanceDay d : days) {
            statusByDate.put(d.date, d.status);
        }

        int totalWorkingDays = countWorkingDays(
                quarter.startDate, quarter.endDate, bankHolidaySet, statusByDate);

        int daysAttended = quarter.preloadCount;
        int daysNotInOffice = 0;
        for (AttendanceDay d : days) {
            if (d.status == DayStatus.IN_OFFICE) {
                daysAttended++;
            } else if (d.status == DayStatus.NOT_IN_OFFICE) {
                daysNotInOffice++;
            }
        }

        float percentage = totalWorkingDays == 0 ? 0f : (float) daysAttended / totalWorkingDays;

        int daysTarget = (int) Math.ceil(quarter.targetPercentage * totalWorkingDays);
        int daysNeeded = Math.max(0, daysTarget - daysAttended);

        LocalDate rangeStart = today.isBefore(quarter.startDate) ? quarter.startDate : today;
        int daysRemaining = countWorkingDays(
                rangeStart, quarter.endDate, bankHolidaySet, statusByDate);

        PaceStatus paceStatus;
        if (daysNeeded == 0) {
            paceStatus = PaceStatus.GREEN;
        } else if (daysNeeded <= daysRemaining * quarter.targetPercentage) {
            paceStatus = PaceStatus.GREEN;
        } else if (daysNeeded <= daysRemaining) {
            paceStatus = PaceStatus.AMBER;
        } else {
            paceStatus = PaceStatus.RED;
        }

        return new QuarterStats(totalWorkingDays, daysAttended, daysNotInOffice, percentage, daysNeeded, daysRemaining, paceStatus);
    }

    private static int countWorkingDays(
            LocalDate from, LocalDate to,
            Set<LocalDate> bankHolidays, Map<LocalDate, DayStatus> statusByDate) {
        int count = 0;
        LocalDate d = from;
        while (!d.isAfter(to)) {
            if (isWeekday(d) && !bankHolidays.contains(d)) {
                DayStatus status = statusByDate.get(d);
                if (status != DayStatus.SICK && status != DayStatus.HOLIDAY && status != DayStatus.BANK_HOLIDAY) {
                    count++;
                }
            }
            d = d.plusDays(1);
        }
        return count;
    }

    private static boolean isWeekday(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }
}
