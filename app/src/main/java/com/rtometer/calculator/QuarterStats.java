package com.rtometer.calculator;

import java.util.List;

public class QuarterStats {
    public final int totalWorkingDays;
    public final int daysAttended;
    public final int daysNotInOffice;
    public final float percentage;
    public final int daysTarget;
    public final int daysNeeded;
    public final int daysRemaining;
    public final PaceStatus paceStatus;
    /** Each element is int[]{workingDayIndex, cumulativeAttendance} up to today. */
    public final List<int[]> burndownSeries;
    /** Each element is int[]{workingDayIndex, monthNumber 1-12} for the first working day of each month in the quarter. */
    public final List<int[]> monthBoundaries;

    public QuarterStats(int totalWorkingDays, int daysAttended, int daysNotInOffice,
                        float percentage, int daysTarget, int daysNeeded, int daysRemaining,
                        PaceStatus paceStatus, List<int[]> burndownSeries, List<int[]> monthBoundaries) {
        this.totalWorkingDays = totalWorkingDays;
        this.daysAttended = daysAttended;
        this.daysNotInOffice = daysNotInOffice;
        this.percentage = percentage;
        this.daysTarget = daysTarget;
        this.daysNeeded = daysNeeded;
        this.daysRemaining = daysRemaining;
        this.paceStatus = paceStatus;
        this.burndownSeries = burndownSeries;
        this.monthBoundaries = monthBoundaries;
    }
}
