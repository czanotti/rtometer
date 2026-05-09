package com.rtometer.history;

import com.rtometer.calculator.QuarterStats;
import com.rtometer.data.db.Quarter;

public class PastQuarterEntry {
    public final Quarter quarter;
    public final QuarterStats stats;

    public PastQuarterEntry(Quarter quarter, QuarterStats stats) {
        this.quarter = quarter;
        this.stats = stats;
    }
}
