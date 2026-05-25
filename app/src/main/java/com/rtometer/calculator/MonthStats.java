package com.rtometer.calculator;

public class MonthStats {
    /** Calendar month 1–12. */
    public final int month;
    /** Total non-holiday working days in this month (within the quarter). */
    public final int workingDays;
    /** IN_OFFICE day count; always 0 for future months. */
    public final int daysAttended;
    /** daysAttended / workingDays; 0 for future months or when workingDays == 0. */
    public final float percentage;
    /** True when the first calendar day of this month is after today. */
    public final boolean isFuture;

    public MonthStats(int month, int workingDays, int daysAttended,
                      float percentage, boolean isFuture) {
        this.month = month;
        this.workingDays = workingDays;
        this.daysAttended = daysAttended;
        this.percentage = percentage;
        this.isFuture = isFuture;
    }
}
