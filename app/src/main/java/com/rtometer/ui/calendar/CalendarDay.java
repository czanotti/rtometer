package com.rtometer.ui.calendar;

import com.rtometer.data.model.DayStatus;

import java.time.LocalDate;

public class CalendarDay {
    public final LocalDate date;
    public final DayStatus status;       // null = no record (future or untracked)
    public final boolean isManualOverride;
    public final boolean isToday;
    public final boolean isWeekend;      // Saturday or Sunday
    public final boolean isBankHoliday;
    public final long attendanceDayId;   // 0 = no DB record

    public CalendarDay(LocalDate date, DayStatus status, boolean isManualOverride,
                       boolean isToday, boolean isWeekend, boolean isBankHoliday,
                       long attendanceDayId) {
        this.date = date;
        this.status = status;
        this.isManualOverride = isManualOverride;
        this.isToday = isToday;
        this.isWeekend = isWeekend;
        this.isBankHoliday = isBankHoliday;
        this.attendanceDayId = attendanceDayId;
    }
}
