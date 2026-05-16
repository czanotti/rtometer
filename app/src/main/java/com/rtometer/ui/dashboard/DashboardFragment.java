package com.rtometer.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rtometer.R;
import com.rtometer.calculator.PaceStatus;
import com.rtometer.data.db.Quarter;
import com.rtometer.ui.main.MainViewModel;

import java.time.format.DateTimeFormatter;
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
        TextView daysOut = view.findViewById(R.id.daysOutCount);
        TextView daysRemaining = view.findViewById(R.id.daysRemainingCount);
        TextView daysNeeded = view.findViewById(R.id.daysNeededCount);

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
            daysOut.setText(String.valueOf(stats.daysNotInOffice));
            daysRemaining.setText(String.valueOf(stats.daysRemaining));
            daysNeeded.setText(String.valueOf(stats.daysNeeded));
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
}
