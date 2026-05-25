package com.rtometer.ui.dashboard;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.MaterialColors;
import com.rtometer.R;
import com.rtometer.calculator.MonthStats;
import com.rtometer.calculator.PaceStatus;
import com.rtometer.calculator.QuarterStats;
import com.rtometer.data.db.Quarter;
import com.rtometer.ui.main.MainViewModel;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardFragment extends Fragment {

    private static final DateTimeFormatter MONTH_DAY = DateTimeFormatter.ofPattern("MMM d");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        View emptyState = view.findViewById(R.id.emptyState);
        Button btnGoToSettings = view.findViewById(R.id.btnGoToSettings);
        btnGoToSettings.setOnClickListener(v -> {
            BottomNavigationView nav = requireActivity().findViewById(R.id.bottomNav);
            nav.setSelectedItemId(R.id.nav_settings);
        });
        View dashboardContent = view.findViewById(R.id.dashboardContent);
        TextView quarterLabel = view.findViewById(R.id.quarterLabel);
        TextView percentageText = view.findViewById(R.id.percentageText);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView targetLabel = view.findViewById(R.id.targetLabel);
        TextView paceChip = view.findViewById(R.id.paceChip);
        TextView daysInOffice = view.findViewById(R.id.daysInOfficeCount);
        TextView daysRemaining = view.findViewById(R.id.daysRemainingCount);
        TextView daysNeeded = view.findViewById(R.id.daysNeededCount);
        BurndownView burndownChart = view.findViewById(R.id.burndownChart);
        LinearLayout monthlyBreakdown = view.findViewById(R.id.monthlyBreakdown);

        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        viewModel.getCurrentQuarter().observe(getViewLifecycleOwner(), quarter -> {
            if (quarter == null) {
                emptyState.setVisibility(View.VISIBLE);
                dashboardContent.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                dashboardContent.setVisibility(View.VISIBLE);
                quarterLabel.setText(formatQuarterLabel(quarter));
                int targetPct = Math.round(quarter.targetPercentage * 100);
                targetLabel.setText(getString(R.string.dashboard_target_label, targetPct));
            }
        });

        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            int pct = Math.round(stats.percentage * 100);
            percentageText.setText(String.format(Locale.getDefault(), "%d%%", pct));
            progressBar.setProgress(pct);
            paceChip.setText(paceLabel(stats.paceStatus));
            daysInOffice.setText(String.valueOf(stats.daysAttended));
            daysRemaining.setText(String.valueOf(stats.daysRemaining));
            daysNeeded.setText(String.valueOf(stats.daysNeeded));
            burndownChart.setData(stats.totalWorkingDays, stats.daysTarget,
                    stats.burndownSeries, stats.monthBoundaries, stats.paceStatus);
            bindMonthlyBreakdown(monthlyBreakdown, stats);
        });
    }

    private String formatQuarterLabel(Quarter q) {
        return getString(R.string.dashboard_quarter_label,
                q.quarterNumber,
                q.startDate.format(MONTH_DAY),
                q.endDate.format(MONTH_DAY));
    }

    private String paceLabel(PaceStatus status) {
        switch (status) {
            case GREEN: return getString(R.string.dashboard_pace_green);
            case AMBER: return getString(R.string.dashboard_pace_amber);
            default:    return getString(R.string.dashboard_pace_red);
        }
    }

    private static final String[] MONTH_SHORT = {
            "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private void bindMonthlyBreakdown(LinearLayout container, QuarterStats stats) {
        List<MonthStats> breakdown = stats.monthBreakdown;
        if (breakdown == null || breakdown.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);

        // Reuse existing row views when possible; inflate new ones if needed.
        int existingCount = container.getChildCount();
        for (int i = 0; i < breakdown.size(); i++) {
            View row;
            if (i < existingCount) {
                row = container.getChildAt(i);
            } else {
                row = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_month_row, container, false);
                container.addView(row);
            }
            bindMonthRow(row, breakdown.get(i), stats.paceStatus);
        }
        // Remove surplus rows if the list shrank.
        while (container.getChildCount() > breakdown.size()) {
            container.removeViewAt(container.getChildCount() - 1);
        }
    }

    private void bindMonthRow(View row, MonthStats m, PaceStatus paceStatus) {
        TextView nameView = row.findViewById(R.id.monthName);
        TextView daysView = row.findViewById(R.id.monthDays);
        TextView pctView  = row.findViewById(R.id.monthPct);
        ProgressBar bar   = row.findViewById(R.id.monthBar);

        nameView.setText(MONTH_SHORT[m.month]);

        if (m.isFuture) {
            daysView.setText(getString(R.string.monthly_breakdown_future_days, m.workingDays));
            pctView.setText(getString(R.string.monthly_breakdown_future_pct));
            bar.setProgress(0);
            bar.setProgressTintList(ColorStateList.valueOf(neutralBarColor()));
        } else {
            daysView.setText(getString(R.string.monthly_breakdown_days_fraction,
                    m.daysAttended, m.workingDays));
            pctView.setText(getString(R.string.monthly_breakdown_pct,
                    Math.round(m.percentage * 100)));
            bar.setProgress(Math.round(m.percentage * 100));
            bar.setProgressTintList(ColorStateList.valueOf(paceBarColor(paceStatus)));
        }
    }

    @ColorInt
    private int paceBarColor(PaceStatus status) {
        int attr;
        switch (status) {
            case GREEN: attr = com.google.android.material.R.attr.colorPrimary; break;
            case AMBER: attr = com.google.android.material.R.attr.colorTertiary; break;
            default:    attr = com.google.android.material.R.attr.colorError; break;
        }
        return MaterialColors.getColor(requireView(), attr, 0xFF9E9E9E);
    }

    @ColorInt
    private int neutralBarColor() {
        return MaterialColors.getColor(requireView(),
                com.google.android.material.R.attr.colorSurfaceVariant, 0xFFE0E0E0);
    }
}
