package com.rtometer.calculator;

import com.rtometer.data.db.Quarter;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class FiscalQuarterFactoryTest {

    // ── CALENDAR (Jan start) ──────────────────────────────────────────────────

    @Test
    public void calendar_todayInMarch_q1StartsJanSameYear() {
        List<Quarter> quarters = FiscalQuarterFactory.build(1, LocalDate.of(2026, 3, 15));

        assertQuarter(quarters.get(0), 1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
        assertQuarter(quarters.get(1), 2, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 30));
        assertQuarter(quarters.get(2), 3, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 31));
    }

    @Test
    public void calendar_todayIsJan_q1StartsJanSameYear() {
        List<Quarter> quarters = FiscalQuarterFactory.build(1, LocalDate.of(2026, 1, 1));

        assertQuarter(quarters.get(0), 1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
    }

    // ── FEB_START ─────────────────────────────────────────────────────────────

    @Test
    public void febStart_todayInMay_q1StartsFebruary() {
        List<Quarter> quarters = FiscalQuarterFactory.build(2, LocalDate.of(2026, 5, 13));

        assertQuarter(quarters.get(0), 1, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 4, 30));
        assertQuarter(quarters.get(1), 2, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 7, 31));
        assertQuarter(quarters.get(2), 3, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 10, 31));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2026, 11, 1), LocalDate.of(2027, 1, 31));
    }

    @Test
    public void febStart_todayInJanuary_cycleStartedPreviousYear() {
        List<Quarter> quarters = FiscalQuarterFactory.build(2, LocalDate.of(2026, 1, 15));

        assertQuarter(quarters.get(0), 1, LocalDate.of(2025, 2, 1), LocalDate.of(2025, 4, 30));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2025, 11, 1), LocalDate.of(2026, 1, 31));
    }

    // ── APR_START ─────────────────────────────────────────────────────────────

    @Test
    public void aprStart_todayInJune_q1StartsAprilSameYear() {
        List<Quarter> quarters = FiscalQuarterFactory.build(4, LocalDate.of(2026, 6, 1));

        assertQuarter(quarters.get(0), 1, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 30));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 3, 31));
    }

    @Test
    public void aprStart_todayInMarch_cycleStartedPreviousYear() {
        List<Quarter> quarters = FiscalQuarterFactory.build(4, LocalDate.of(2026, 3, 31));

        assertQuarter(quarters.get(0), 1, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 6, 30));
    }

    // ── MAY_START ─────────────────────────────────────────────────────────────

    @Test
    public void mayStart_todayInMay_q1StartsMaySameYear() {
        List<Quarter> quarters = FiscalQuarterFactory.build(5, LocalDate.of(2026, 5, 1));

        assertQuarter(quarters.get(0), 1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 7, 31));
    }

    // ── CUSTOM preset ─────────────────────────────────────────────────────────

    @Test
    public void custom_invalidMonth_throws() {
        assertThrows(IllegalArgumentException.class, () -> FiscalQuarterFactory.createCustom(0));
        assertThrows(IllegalArgumentException.class, () -> FiscalQuarterFactory.createCustom(13));
    }

    @Test
    public void create_withCustomPreset_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> FiscalQuarterFactory.create(FiscalQuarterPreset.CUSTOM));
    }

    // ── Four quarters, no gaps ────────────────────────────────────────────────

    @Test
    public void build_fourQuartersCoverTwelveMonths() {
        List<Quarter> quarters = FiscalQuarterFactory.build(2, LocalDate.of(2026, 5, 13));

        assertEquals(4, quarters.size());
        LocalDate cycleStart = quarters.get(0).startDate;
        LocalDate cycleEnd = quarters.get(3).endDate;
        assertEquals(cycleStart.plusMonths(12).minusDays(1), cycleEnd);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void assertQuarter(Quarter q, int number, LocalDate start, LocalDate end) {
        assertEquals("quarterNumber", number, q.quarterNumber);
        assertEquals("startDate", start, q.startDate);
        assertEquals("endDate", end, q.endDate);
    }
}
