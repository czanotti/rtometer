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

    public QuarterStats(int totalWorkingDays, int daysAttended, int daysNotInOffice,
                        float percentage, int daysTarget, int daysNeeded, int daysRemaining,
                        PaceStatus paceStatus, List<int[]> burndownSeries) {
        this.totalWorkingDays = totalWorkingDays;
        this.daysAttended = daysAttended;
        this.daysNotInOffice = daysNotInOffice;
        this.percentage = percentage;
        this.daysTarget = daysTarget;
        this.daysNeeded = daysNeeded;
        this.daysRemaining = daysRemaining;
        this.paceStatus = paceStatus;
        this.burndownSeries = burndownSeries;
    }
}
