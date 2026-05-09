package com.rtometer.calculator;

import com.rtometer.data.db.Quarter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FiscalQuarterFactory {

    public static List<Quarter> create(FiscalQuarterPreset preset, int calendarYear, int fiscalYearOffset) {
        if (preset == FiscalQuarterPreset.CUSTOM) {
            throw new IllegalArgumentException("Use createCustom() for CUSTOM preset");
        }
        return build(preset.startMonth, calendarYear, fiscalYearOffset);
    }

    public static List<Quarter> createCustom(int startMonth, int calendarYear, int fiscalYearOffset) {
        if (startMonth < 1 || startMonth > 12) {
            throw new IllegalArgumentException("startMonth must be 1–12");
        }
        return build(startMonth, calendarYear, fiscalYearOffset);
    }

    private static List<Quarter> build(int startMonth, int calendarYear, int fiscalYearOffset) {
        int fiscalYear = calendarYear + fiscalYearOffset;
        LocalDate q1Start = LocalDate.of(calendarYear, startMonth, 1);
        List<Quarter> quarters = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            LocalDate start = q1Start.plusMonths(3L * i);
            LocalDate end = q1Start.plusMonths(3L * (i + 1)).minusDays(1);
            Quarter q = new Quarter();
            q.fiscalYear = fiscalYear;
            q.quarterNumber = i + 1;
            q.startDate = start;
            q.endDate = end;
            quarters.add(q);
        }
        return quarters;
    }
}
