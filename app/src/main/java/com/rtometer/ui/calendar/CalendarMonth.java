package com.rtometer.ui.calendar;

import java.time.YearMonth;
import java.util.List;

public class CalendarMonth {
    public final YearMonth month;
    /** All cells for this month, Mon-origin aligned. Null entries are empty padding slots. */
    public final List<CalendarDay> days;

    public CalendarMonth(YearMonth month, List<CalendarDay> days) {
        this.month = month;
        this.days = days;
    }
}
