package com.rtometer.calculator;

import com.rtometer.data.db.AttendanceDay;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.model.DayStatus;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QuarterCalculatorTest {

    // Jan 6–17 2025: 2 full Mon–Fri weeks = 10 working days, target 50%
    private Quarter twoWeekQuarter() {
        Quarter q = new Quarter();
        q.quarterNumber = 1;
        q.startDate = LocalDate.of(2025, 1, 6);
        q.endDate = LocalDate.of(2025, 1, 17);
        q.targetPercentage = 0.5f;
        return q;
    }

    private AttendanceDay day(Quarter q, LocalDate date, DayStatus status) {
        AttendanceDay d = new AttendanceDay();
        d.date = date;
        d.quarterId = q.id;
        d.status = status;
        return d;
    }

    @Test
    public void totalWorkingDays_countsMondayToFridayOnly() {
        QuarterStats s = QuarterCalculator.calculate(twoWeekQuarter(), Collections.emptyList(), Collections.emptyList(), LocalDate.of(2025, 1, 6));
        assertEquals(10, s.totalWorkingDays);
    }

    @Test
    public void totalWorkingDays_excludesBankHolidays() {
        QuarterStats s = QuarterCalculator.calculate(
                twoWeekQuarter(),
                Collections.emptyList(),
                List.of(LocalDate.of(2025, 1, 6)),
                LocalDate.of(2025, 1, 6));
        assertEquals(9, s.totalWorkingDays);
    }

    @Test
    public void totalWorkingDays_excludesBankHolidayStatus() {
        Quarter q = twoWeekQuarter();
        QuarterStats s = QuarterCalculator.calculate(
                q,
                List.of(day(q, LocalDate.of(2025, 1, 6), DayStatus.BANK_HOLIDAY)),
                Collections.emptyList(),
                LocalDate.of(2025, 1, 6));
        assertEquals(9, s.totalWorkingDays);
    }

    @Test
    public void totalWorkingDays_excludesSickDays() {
        Quarter q = twoWeekQuarter();
        QuarterStats s = QuarterCalculator.calculate(
                q,
                List.of(day(q, LocalDate.of(2025, 1, 7), DayStatus.SICK)),
                Collections.emptyList(),
                LocalDate.of(2025, 1, 6));
        assertEquals(9, s.totalWorkingDays);
    }

    @Test
    public void totalWorkingDays_excludesHolidayDays() {
        Quarter q = twoWeekQuarter();
        QuarterStats s = QuarterCalculator.calculate(
                q,
                List.of(day(q, LocalDate.of(2025, 1, 8), DayStatus.HOLIDAY)),
                Collections.emptyList(),
                LocalDate.of(2025, 1, 6));
        assertEquals(9, s.totalWorkingDays);
    }

    @Test
    public void fy27q1_has66WorkingDays() {
        // May 1 – Jul 31 2026: 21 + 22 + 23 = 66 weekdays, no bank holidays
        Quarter q = new Quarter();
        q.quarterNumber = 1;
        q.startDate = LocalDate.of(2026, 5, 1);
        q.endDate = LocalDate.of(2026, 7, 31);
        q.targetPercentage = 0.5f;
        QuarterStats s = QuarterCalculator.calculate(q, Collections.emptyList(), Collections.emptyList(), LocalDate.of(2026, 5, 1));
        assertEquals(66, s.totalWorkingDays);
    }

    @Test
    public void sickDayExcludedFromDenominator() {
        Quarter q = twoWeekQuarter(); // 9 effective working days after sick exclusion
        List<AttendanceDay> days = List.of(
                day(q, LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 7), DayStatus.SICK)
        );
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 6));
        assertEquals(9, s.totalWorkingDays);
        assertEquals(1, s.daysAttended);
    }

    @Test
    public void holidayDayExcludedFromDenominator() {
        Quarter q = twoWeekQuarter(); // 9 effective working days after holiday exclusion
        List<AttendanceDay> days = List.of(
                day(q, LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 7), DayStatus.HOLIDAY)
        );
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 6));
        assertEquals(9, s.totalWorkingDays);
        assertEquals(1, s.daysAttended);
    }

    @Test
    public void daysRemaining_excludesFutureSickDay() {
        // today = Jan 13 (Mon), daysRemaining normally 5 (Jan 13–17)
        // Jan 14 pre-marked SICK → daysRemaining = 4
        Quarter q = twoWeekQuarter();
        List<AttendanceDay> days = List.of(day(q, LocalDate.of(2025, 1, 14), DayStatus.SICK));
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 13));
        assertEquals(4, s.daysRemaining);
    }

    @Test
    public void daysRemaining_excludesFutureHolidayDay() {
        // today = Jan 13 (Mon), daysRemaining normally 5 (Jan 13–17)
        // Jan 14 pre-marked HOLIDAY → daysRemaining = 4
        Quarter q = twoWeekQuarter();
        List<AttendanceDay> days = List.of(day(q, LocalDate.of(2025, 1, 14), DayStatus.HOLIDAY));
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 13));
        assertEquals(4, s.daysRemaining);
    }

    @Test
    public void paceStatus_red_whenFutureSickDaysLeaveInsufficientRemaining() {
        // today = Jan 13, Jan 14–17 all SICK → daysRemaining=1 (only Jan 13)
        // totalWorkingDays = 10 - 4 = 6, daysTarget = ceil(0.5*6)=3, daysNeeded=3
        // 3 > 1 → RED (without fix: daysRemaining=5, daysNeeded=5, 5<=5 → AMBER)
        Quarter q = twoWeekQuarter();
        List<AttendanceDay> days = List.of(
                day(q, LocalDate.of(2025, 1, 14), DayStatus.SICK),
                day(q, LocalDate.of(2025, 1, 15), DayStatus.SICK),
                day(q, LocalDate.of(2025, 1, 16), DayStatus.SICK),
                day(q, LocalDate.of(2025, 1, 17), DayStatus.SICK)
        );
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 13));
        assertEquals(PaceStatus.RED, s.paceStatus);
    }

    @Test
    public void totalWorkingDays_includesClearDay() {
        Quarter q = twoWeekQuarter();
        QuarterStats s = QuarterCalculator.calculate(
                q,
                List.of(day(q, LocalDate.of(2025, 1, 6), DayStatus.CLEAR)),
                Collections.emptyList(),
                LocalDate.of(2025, 1, 6));
        assertEquals(10, s.totalWorkingDays);
    }

    @Test
    public void daysAttended_doesNotCountClearDay() {
        Quarter q = twoWeekQuarter();
        QuarterStats s = QuarterCalculator.calculate(
                q,
                List.of(day(q, LocalDate.of(2025, 1, 6), DayStatus.CLEAR)),
                Collections.emptyList(),
                LocalDate.of(2025, 1, 6));
        assertEquals(0, s.daysAttended);
    }

    @Test
    public void daysAttended_countsInOfficeDays() {
        Quarter q = twoWeekQuarter();
        List<AttendanceDay> days = List.of(
                day(q, LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 7), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 8), DayStatus.NOT_IN_OFFICE)
        );
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 6));
        assertEquals(2, s.daysAttended);
    }

    @Test
    public void percentage_calculatedCorrectly() {
        Quarter q = twoWeekQuarter(); // 10 working days
        List<AttendanceDay> days = List.of(
                day(q, LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 7), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 8), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 9), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 10), DayStatus.IN_OFFICE)
        );
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 6));
        assertEquals(0.5f, s.percentage, 0.001f);
    }

    @Test
    public void percentage_zeroWhenNoWorkingDays() {
        Quarter q = new Quarter();
        q.startDate = LocalDate.of(2025, 1, 11); // Saturday
        q.endDate = LocalDate.of(2025, 1, 12);   // Sunday
        q.targetPercentage = 0.5f;
        QuarterStats s = QuarterCalculator.calculate(q, Collections.emptyList(), Collections.emptyList(), LocalDate.of(2025, 1, 11));
        assertEquals(0, s.totalWorkingDays);
        assertEquals(0f, s.percentage, 0.001f);
    }

    @Test
    public void daysNeeded_calculatedCorrectly() {
        // target 50%, 10 days → need ceil(5) = 5; attended 3 → need 2 more
        Quarter q = twoWeekQuarter();
        List<AttendanceDay> days = List.of(
                day(q, LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 7), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 8), DayStatus.IN_OFFICE)
        );
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 6));
        assertEquals(2, s.daysNeeded);
    }

    @Test
    public void daysNeeded_zeroWhenTargetMet() {
        Quarter q = twoWeekQuarter();
        List<AttendanceDay> days = List.of(
                day(q, LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 7), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 8), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 9), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 10), DayStatus.IN_OFFICE)
        );
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 6));
        assertEquals(0, s.daysNeeded);
    }

    @Test
    public void daysRemaining_countsFutureWorkingDaysIncludingToday() {
        // today = Jan 13 (Mon, week 2 start) → remaining = 5 (Jan 13–17)
        QuarterStats s = QuarterCalculator.calculate(twoWeekQuarter(), Collections.emptyList(), Collections.emptyList(), LocalDate.of(2025, 1, 13));
        assertEquals(5, s.daysRemaining);
    }

    @Test
    public void paceStatus_green_whenAheadOfPace() {
        // today = Jan 13, daysRemaining = 5, attended 4, daysNeeded = 1
        // 1 <= 5 * 0.5 = 2.5 → GREEN
        Quarter q = twoWeekQuarter();
        List<AttendanceDay> days = List.of(
                day(q, LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 7), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 8), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 9), DayStatus.IN_OFFICE)
        );
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 13));
        assertEquals(PaceStatus.GREEN, s.paceStatus);
    }

    @Test
    public void paceStatus_green_whenTargetAlreadyMet() {
        Quarter q = twoWeekQuarter();
        List<AttendanceDay> days = List.of(
                day(q, LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 7), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 8), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 9), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 10), DayStatus.IN_OFFICE)
        );
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 13));
        assertEquals(PaceStatus.GREEN, s.paceStatus);
    }

    @Test
    public void paceStatus_amber_whenAchievableButTight() {
        // today = Jan 13, daysRemaining = 5, attended 2, daysNeeded = 3
        // 3 > 5 * 0.5 = 2.5 → not GREEN; 3 <= 5 → AMBER
        Quarter q = twoWeekQuarter();
        List<AttendanceDay> days = List.of(
                day(q, LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 7), DayStatus.IN_OFFICE)
        );
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 13));
        assertEquals(PaceStatus.AMBER, s.paceStatus);
    }

    @Test
    public void paceStatus_red_whenImpossible() {
        // today = Jan 16, daysRemaining = 2 (Jan 16–17), attended 0, daysNeeded = 5
        // 5 > 2 → RED
        QuarterStats s = QuarterCalculator.calculate(twoWeekQuarter(), Collections.emptyList(), Collections.emptyList(), LocalDate.of(2025, 1, 16));
        assertEquals(PaceStatus.RED, s.paceStatus);
    }

    // May–Jul 2026 quarter (Q2 FY2026, 66 working days)
    private Quarter mayJulQuarter() {
        Quarter q = new Quarter();
        q.quarterNumber = 2;
        q.startDate = LocalDate.of(2026, 5, 1);
        q.endDate = LocalDate.of(2026, 7, 31);
        q.targetPercentage = 0.5f;
        return q;
    }

    @Test
    public void daysRemaining_mayJulQuarter_66_whenTodayIsFirstDay() {
        // May 1 (Fri) is first day of quarter → all 66 working days remain
        QuarterStats s = QuarterCalculator.calculate(mayJulQuarter(), Collections.emptyList(), Collections.emptyList(), LocalDate.of(2026, 5, 1));
        assertEquals(66, s.daysRemaining);
    }

    @Test
    public void daysRemaining_mayJulQuarter_45_whenTodayIsJune1() {
        // Jun 1 (Mon) → June + July = 22 + 23 = 45 working days remain
        QuarterStats s = QuarterCalculator.calculate(mayJulQuarter(), Collections.emptyList(), Collections.emptyList(), LocalDate.of(2026, 6, 1));
        assertEquals(45, s.daysRemaining);
    }

    @Test
    public void daysRemaining_mayJulQuarter_23_whenTodayIsJuly1() {
        // Jul 1 (Wed) → July only = 23 working days remain
        QuarterStats s = QuarterCalculator.calculate(mayJulQuarter(), Collections.emptyList(), Collections.emptyList(), LocalDate.of(2026, 7, 1));
        assertEquals(23, s.daysRemaining);
    }

    // --- Burndown series tests ---

    @Test
    public void burndownSeries_emptyWhenNoDaysElapsed() {
        // today is before quarter start → no working days elapsed → empty series
        Quarter q = twoWeekQuarter(); // starts Jan 6 2025
        QuarterStats s = QuarterCalculator.calculate(q, Collections.emptyList(), Collections.emptyList(), LocalDate.of(2025, 1, 5));
        assertEquals(0, s.burndownSeries.size());
    }

    @Test
    public void burndownSeries_firstDayInOffice_onePoint() {
        // today = Jan 6, attended → series has 1 point: dayIndex=1, cumulative=1
        Quarter q = twoWeekQuarter();
        List<AttendanceDay> days = List.of(day(q, LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE));
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 6));
        assertEquals(1, s.burndownSeries.size());
        assertEquals(1, s.burndownSeries.get(0)[0]); // dayIndex
        assertEquals(1, s.burndownSeries.get(0)[1]); // cumulative
    }

    @Test
    public void burndownSeries_notInOffice_cumulativeDoesNotIncrease() {
        // Jan 6 NOT_IN_OFFICE, Jan 7 IN_OFFICE → cumulative = [0, 1]
        Quarter q = twoWeekQuarter();
        List<AttendanceDay> days = List.of(
                day(q, LocalDate.of(2025, 1, 6), DayStatus.NOT_IN_OFFICE),
                day(q, LocalDate.of(2025, 1, 7), DayStatus.IN_OFFICE)
        );
        QuarterStats s = QuarterCalculator.calculate(q, days, Collections.emptyList(), LocalDate.of(2025, 1, 7));
        assertEquals(2, s.burndownSeries.size());
        assertEquals(0, s.burndownSeries.get(0)[1]); // day 1 cumulative = 0
        assertEquals(1, s.burndownSeries.get(1)[1]); // day 2 cumulative = 1
    }

    @Test
    public void burndownSeries_unrecordedDayCountsAsZero() {
        // today = Jan 7, no days recorded → 2 points with cumulative=0 each
        Quarter q = twoWeekQuarter();
        QuarterStats s = QuarterCalculator.calculate(q, Collections.emptyList(), Collections.emptyList(), LocalDate.of(2025, 1, 7));
        assertEquals(2, s.burndownSeries.size());
        assertEquals(0, s.burndownSeries.get(0)[1]);
        assertEquals(0, s.burndownSeries.get(1)[1]);
    }

    @Test
    public void burndownSeries_stopsAtQuarterEnd() {
        // today is after quarter end → series covers all 10 working days only
        Quarter q = twoWeekQuarter(); // Jan 6–17
        QuarterStats s = QuarterCalculator.calculate(q, Collections.emptyList(), Collections.emptyList(), LocalDate.of(2025, 1, 20));
        assertEquals(10, s.burndownSeries.size());
    }

    @Test
    public void burndownSeries_excludesBankHolidayFromSeries() {
        // Jan 6 is bank holiday → today=Jan 7 → series has 1 point (Jan 7 only)
        Quarter q = twoWeekQuarter();
        QuarterStats s = QuarterCalculator.calculate(q, Collections.emptyList(),
                List.of(LocalDate.of(2025, 1, 6)), LocalDate.of(2025, 1, 7));
        assertEquals(1, s.burndownSeries.size());
        assertEquals(1, s.burndownSeries.get(0)[0]); // dayIndex = 1
    }

    @Test
    public void daysTarget_calculatedCorrectly() {
        // 10 working days, 50% target → daysTarget = ceil(5) = 5
        QuarterStats s = QuarterCalculator.calculate(twoWeekQuarter(), Collections.emptyList(), Collections.emptyList(), LocalDate.of(2025, 1, 6));
        assertEquals(5, s.daysTarget);
    }
}
