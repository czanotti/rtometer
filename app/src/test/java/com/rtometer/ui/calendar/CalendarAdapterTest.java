package com.rtometer.ui.calendar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import com.rtometer.R;
import com.rtometer.data.model.DayStatus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class CalendarAdapterTest {

    private Context ctx;

    @Before
    public void setup() {
        ctx = ApplicationProvider.getApplicationContext();
    }

    private CalendarAdapter.DayHolder createHolder() {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_calendar_day, null, false);
        return new CalendarAdapter.DayHolder(v);
    }

    private CalendarDay day(LocalDate date, DayStatus status, boolean isWeekend, boolean isBankHoliday) {
        return new CalendarDay(date, status, false, false, isWeekend, isBankHoliday, 0);
    }

    @Test
    public void bind_inOfficeDay_hasBlackText() {
        CalendarAdapter.DayHolder h = createHolder();
        h.bind(day(LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE, false, false),
                null, false, Collections.emptySet(), null);
        assertEquals(Color.BLACK, h.dayNumber.getCurrentTextColor());
    }

    @Test
    public void bind_sickDay_hasBlackText() {
        CalendarAdapter.DayHolder h = createHolder();
        h.bind(day(LocalDate.of(2025, 1, 6), DayStatus.SICK, false, false),
                null, false, Collections.emptySet(), null);
        assertEquals(Color.BLACK, h.dayNumber.getCurrentTextColor());
    }

    @Test
    public void bind_holidayDay_hasBlackText() {
        CalendarAdapter.DayHolder h = createHolder();
        h.bind(day(LocalDate.of(2025, 1, 6), DayStatus.HOLIDAY, false, false),
                null, false, Collections.emptySet(), null);
        assertEquals(Color.BLACK, h.dayNumber.getCurrentTextColor());
    }

    @Test
    public void bind_bankHolidayDay_hasBlackText() {
        CalendarAdapter.DayHolder h = createHolder();
        h.bind(day(LocalDate.of(2025, 1, 1), null, false, true),
                null, false, Collections.emptySet(), null);
        assertEquals(Color.BLACK, h.dayNumber.getCurrentTextColor());
    }

    @Test
    public void bind_weekendDay_hasBlackText() {
        CalendarAdapter.DayHolder h = createHolder();
        h.bind(day(LocalDate.of(2025, 1, 4), null, true, false),
                null, false, Collections.emptySet(), null);
        assertEquals(Color.BLACK, h.dayNumber.getCurrentTextColor());
    }

    @Test
    public void bind_bulkSelectedDay_hasBlackText() {
        CalendarAdapter.DayHolder h = createHolder();
        Set<LocalDate> selected = new HashSet<>();
        selected.add(LocalDate.of(2025, 1, 6));
        h.bind(day(LocalDate.of(2025, 1, 6), null, false, false),
                null, true, selected, null);
        assertEquals(Color.BLACK, h.dayNumber.getCurrentTextColor());
    }

    @Test
    public void bind_noStatusTransparent_usesDefaultTextColor() {
        CalendarAdapter.DayHolder h = createHolder();
        int defaultColor = h.defaultTextColor;
        h.bind(day(LocalDate.of(2025, 1, 6), null, false, false),
                null, false, Collections.emptySet(), null);
        assertEquals(defaultColor, h.dayNumber.getCurrentTextColor());
    }
}
