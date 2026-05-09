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
        q.fiscalYear = 2025;
        q.quarterNumber = 1;
        q.startDate = LocalDate.of(2025, 1, 6);
        q.endDate = LocalDate.of(2025, 1, 17);
        q.targetPercentage = 0.5f;
        q.preloadCount = 0;
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
    public void totalWorkingDays_excludesPersonalHolidays() {
        Quarter q = twoWeekQuarter();
        QuarterStats s = QuarterCalculator.calculate(
                q,
                List.of(day(q, LocalDate.of(2025, 1, 8), DayStatus.HOLIDAY)),
                Collections.emptyList(),
                LocalDate.of(2025, 1, 6));
        assertEquals(9, s.totalWorkingDays);
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
    public void daysAttended_includesPreloadCount() {
        Quarter q = twoWeekQuarter();
        q.preloadCount = 3;
        QuarterStats s = QuarterCalculator.calculate(
                q,
                List.of(day(q, LocalDate.of(2025, 1, 6), DayStatus.IN_OFFICE)),
                Collections.emptyList(),
                LocalDate.of(2025, 1, 6));
        assertEquals(4, s.daysAttended);
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
}
