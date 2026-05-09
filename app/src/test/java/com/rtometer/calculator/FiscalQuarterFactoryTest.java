package com.rtometer.calculator;

import com.rtometer.data.db.Quarter;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class FiscalQuarterFactoryTest {

    // ── CALENDAR preset ──────────────────────────────────────────────────────

    @Test
    public void calendar_2024_offset0_correctDates() {
        List<Quarter> quarters = FiscalQuarterFactory.create(FiscalQuarterPreset.CALENDAR, 2024, 0);

        assertEquals(2024, quarters.get(0).fiscalYear);
        assertQuarter(quarters.get(0), 1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));
        assertQuarter(quarters.get(1), 2, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 6, 30));
        assertQuarter(quarters.get(2), 3, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 9, 30));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2024, 10, 1), LocalDate.of(2024, 12, 31));
    }

    @Test
    public void calendar_2025_offset0_correctDates() {
        List<Quarter> quarters = FiscalQuarterFactory.create(FiscalQuarterPreset.CALENDAR, 2025, 0);

        assertEquals(2025, quarters.get(0).fiscalYear);
        assertQuarter(quarters.get(0), 1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        assertQuarter(quarters.get(1), 2, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 6, 30));
        assertQuarter(quarters.get(2), 3, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 9, 30));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2025, 10, 1), LocalDate.of(2025, 12, 31));
    }

    // ── FEB_START preset ─────────────────────────────────────────────────────

    @Test
    public void febStart_2024_offset0_correctDates() {
        List<Quarter> quarters = FiscalQuarterFactory.create(FiscalQuarterPreset.FEB_START, 2024, 0);

        assertEquals(2024, quarters.get(0).fiscalYear);
        assertQuarter(quarters.get(0), 1, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 4, 30));
        assertQuarter(quarters.get(1), 2, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 7, 31));
        assertQuarter(quarters.get(2), 3, LocalDate.of(2024, 8, 1), LocalDate.of(2024, 10, 31));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2024, 11, 1), LocalDate.of(2025, 1, 31));
    }

    @Test
    public void febStart_2023_offset0_correctDates() {
        List<Quarter> quarters = FiscalQuarterFactory.create(FiscalQuarterPreset.FEB_START, 2023, 0);

        assertEquals(2023, quarters.get(0).fiscalYear);
        assertQuarter(quarters.get(0), 1, LocalDate.of(2023, 2, 1), LocalDate.of(2023, 4, 30));
        assertQuarter(quarters.get(1), 2, LocalDate.of(2023, 5, 1), LocalDate.of(2023, 7, 31));
        assertQuarter(quarters.get(2), 3, LocalDate.of(2023, 8, 1), LocalDate.of(2023, 10, 31));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2023, 11, 1), LocalDate.of(2024, 1, 31));
    }

    // ── APR_START preset ─────────────────────────────────────────────────────

    @Test
    public void aprStart_2024_offset1_correctDates() {
        List<Quarter> quarters = FiscalQuarterFactory.create(FiscalQuarterPreset.APR_START, 2024, 1);

        assertEquals(2025, quarters.get(0).fiscalYear);
        assertQuarter(quarters.get(0), 1, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 6, 30));
        assertQuarter(quarters.get(1), 2, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 9, 30));
        assertQuarter(quarters.get(2), 3, LocalDate.of(2024, 10, 1), LocalDate.of(2024, 12, 31));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
    }

    @Test
    public void aprStart_2023_offset1_correctDates() {
        List<Quarter> quarters = FiscalQuarterFactory.create(FiscalQuarterPreset.APR_START, 2023, 1);

        assertEquals(2024, quarters.get(0).fiscalYear);
        assertQuarter(quarters.get(0), 1, LocalDate.of(2023, 4, 1), LocalDate.of(2023, 6, 30));
        assertQuarter(quarters.get(1), 2, LocalDate.of(2023, 7, 1), LocalDate.of(2023, 9, 30));
        assertQuarter(quarters.get(2), 3, LocalDate.of(2023, 10, 1), LocalDate.of(2023, 12, 31));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));
    }

    // ── MAY_START preset ─────────────────────────────────────────────────────

    @Test
    public void mayStart_2024_offset1_correctDates() {
        List<Quarter> quarters = FiscalQuarterFactory.create(FiscalQuarterPreset.MAY_START, 2024, 1);

        assertEquals(2025, quarters.get(0).fiscalYear);
        assertQuarter(quarters.get(0), 1, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 7, 31));
        assertQuarter(quarters.get(1), 2, LocalDate.of(2024, 8, 1), LocalDate.of(2024, 10, 31));
        assertQuarter(quarters.get(2), 3, LocalDate.of(2024, 11, 1), LocalDate.of(2025, 1, 31));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2025, 2, 1), LocalDate.of(2025, 4, 30));
    }

    @Test
    public void mayStart_2023_offset1_correctDates() {
        List<Quarter> quarters = FiscalQuarterFactory.create(FiscalQuarterPreset.MAY_START, 2023, 1);

        assertEquals(2024, quarters.get(0).fiscalYear);
        assertQuarter(quarters.get(0), 1, LocalDate.of(2023, 5, 1), LocalDate.of(2023, 7, 31));
        assertQuarter(quarters.get(1), 2, LocalDate.of(2023, 8, 1), LocalDate.of(2023, 10, 31));
        assertQuarter(quarters.get(2), 3, LocalDate.of(2023, 11, 1), LocalDate.of(2024, 1, 31));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 4, 30));
    }

    // ── CUSTOM preset ─────────────────────────────────────────────────────────

    @Test
    public void custom_octStart_2023_offset1_correctDates() {
        List<Quarter> quarters = FiscalQuarterFactory.createCustom(10, 2023, 1);

        assertEquals(2024, quarters.get(0).fiscalYear);
        assertQuarter(quarters.get(0), 1, LocalDate.of(2023, 10, 1), LocalDate.of(2023, 12, 31));
        assertQuarter(quarters.get(1), 2, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));
        assertQuarter(quarters.get(2), 3, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 6, 30));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 9, 30));
    }

    @Test
    public void custom_octStart_2022_offset1_correctDates() {
        List<Quarter> quarters = FiscalQuarterFactory.createCustom(10, 2022, 1);

        assertEquals(2023, quarters.get(0).fiscalYear);
        assertQuarter(quarters.get(0), 1, LocalDate.of(2022, 10, 1), LocalDate.of(2022, 12, 31));
        assertQuarter(quarters.get(1), 2, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 3, 31));
        assertQuarter(quarters.get(2), 3, LocalDate.of(2023, 4, 1), LocalDate.of(2023, 6, 30));
        assertQuarter(quarters.get(3), 4, LocalDate.of(2023, 7, 1), LocalDate.of(2023, 9, 30));
    }

    @Test
    public void custom_invalidMonth_throws() {
        assertThrows(IllegalArgumentException.class, () -> FiscalQuarterFactory.createCustom(0, 2024, 0));
        assertThrows(IllegalArgumentException.class, () -> FiscalQuarterFactory.createCustom(13, 2024, 0));
    }

    @Test
    public void create_withCustomPreset_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> FiscalQuarterFactory.create(FiscalQuarterPreset.CUSTOM, 2024, 0));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void assertQuarter(Quarter q, int number, LocalDate start, LocalDate end) {
        assertEquals("quarterNumber", number, q.quarterNumber);
        assertEquals("startDate", start, q.startDate);
        assertEquals("endDate", end, q.endDate);
    }
}
