package com.rtometer.calculator;

public class QuarterStats {
    public final int totalWorkingDays;
    public final int daysAttended;
    public final float percentage;
    public final int daysNeeded;
    public final int daysRemaining;
    public final PaceStatus paceStatus;

    public QuarterStats(int totalWorkingDays, int daysAttended, float percentage,
                        int daysNeeded, int daysRemaining, PaceStatus paceStatus) {
        this.totalWorkingDays = totalWorkingDays;
        this.daysAttended = daysAttended;
        this.percentage = percentage;
        this.daysNeeded = daysNeeded;
        this.daysRemaining = daysRemaining;
        this.paceStatus = paceStatus;
    }
}
