package com.rtometer.ui.history;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.rtometer.R;
import com.rtometer.calculator.PaceStatus;
import com.rtometer.calculator.QuarterStats;
import com.rtometer.data.db.Quarter;
import com.rtometer.history.PastQuarterEntry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class HistoryAdapterClickTest {

    private Context themedContext() {
        Context ctx = ApplicationProvider.getApplicationContext();
        ctx.setTheme(R.style.Theme_RTOmeter);
        return ctx;
    }

    @Test
    public void clickRow_invokesListenerWithCorrectEntry() {
        Context ctx = themedContext();

        Quarter q = new Quarter();
        q.id = 42L;
        q.quarterNumber = 2;
        q.startDate = LocalDate.of(2025, 1, 1);
        q.endDate = LocalDate.of(2025, 3, 31);
        q.targetPercentage = 0.5f;

        QuarterStats stats = new QuarterStats(
                65, 30, 35, 0.46f, 33, 3, 0,
                PaceStatus.RED, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        PastQuarterEntry entry = new PastQuarterEntry(q, stats);

        AtomicReference<PastQuarterEntry> captured = new AtomicReference<>();
        HistoryAdapter adapter = new HistoryAdapter();
        adapter.setOnQuarterClickListener(captured::set);
        adapter.setEntries(List.of(entry));

        RecyclerView recycler = new RecyclerView(ctx);
        recycler.setLayoutManager(new LinearLayoutManager(ctx));
        recycler.setAdapter(adapter);

        int w = android.view.View.MeasureSpec.makeMeasureSpec(1080, android.view.View.MeasureSpec.EXACTLY);
        int h = android.view.View.MeasureSpec.makeMeasureSpec(1920, android.view.View.MeasureSpec.EXACTLY);
        recycler.measure(w, h);
        recycler.layout(0, 0, 1080, 1920);

        recycler.getChildAt(0).performClick();

        assertNotNull(captured.get());
        assertEquals(42L, captured.get().quarter.id);
    }

    @Test
    public void noListenerSet_clickDoesNotCrash() {
        Context ctx = themedContext();

        Quarter q = new Quarter();
        q.id = 1L;
        q.quarterNumber = 1;
        q.startDate = LocalDate.of(2025, 1, 1);
        q.endDate = LocalDate.of(2025, 3, 31);
        q.targetPercentage = 0.5f;

        QuarterStats stats = new QuarterStats(
                65, 30, 35, 0.46f, 33, 3, 0,
                PaceStatus.GREEN, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        HistoryAdapter adapter = new HistoryAdapter();
        adapter.setEntries(List.of(new PastQuarterEntry(q, stats)));

        RecyclerView recycler = new RecyclerView(ctx);
        recycler.setLayoutManager(new LinearLayoutManager(ctx));
        recycler.setAdapter(adapter);

        int w = android.view.View.MeasureSpec.makeMeasureSpec(1080, android.view.View.MeasureSpec.EXACTLY);
        int h = android.view.View.MeasureSpec.makeMeasureSpec(1920, android.view.View.MeasureSpec.EXACTLY);
        recycler.measure(w, h);
        recycler.layout(0, 0, 1080, 1920);

        recycler.getChildAt(0).performClick(); // must not throw
    }
}
