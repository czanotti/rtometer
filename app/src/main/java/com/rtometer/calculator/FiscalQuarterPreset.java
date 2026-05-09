package com.rtometer.calculator;

public enum FiscalQuarterPreset {
    CALENDAR(1),
    FEB_START(2),
    APR_START(4),
    MAY_START(5),
    CUSTOM(-1);

    public final int startMonth;

    FiscalQuarterPreset(int startMonth) {
        this.startMonth = startMonth;
    }
}
