package com.rtometer.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rtometer.R;
import com.rtometer.calculator.PaceStatus;
import com.rtometer.calculator.QuarterStats;
import com.rtometer.data.db.Quarter;
import com.rtometer.ui.onboarding.OnboardingActivity;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private static final DateTimeFormatter MONTH_DAY = DateTimeFormatter.ofPattern("MMM d");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean onboardingDone = getSharedPreferences("rtometer", MODE_PRIVATE)
                .getBoolean("onboarding_complete", false);
        if (!onboardingDone) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        View emptyState = findViewById(R.id.emptyState);
        View dashboardContent = findViewById(R.id.dashboardContent);
        TextView quarterLabel = findViewById(R.id.quarterLabel);
        TextView percentageText = findViewById(R.id.percentageText);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView targetLabel = findViewById(R.id.targetLabel);
        TextView paceChip = findViewById(R.id.paceChip);
        TextView daysInOffice = findViewById(R.id.daysInOfficeCount);
        TextView daysOut = findViewById(R.id.daysOutCount);
        TextView daysRemaining = findViewById(R.id.daysRemainingCount);
        TextView daysNeeded = findViewById(R.id.daysNeededCount);

        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getCurrentQuarter().observe(this, quarter -> {
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

        viewModel.getStats().observe(this, stats -> {
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
                q.fiscalYear,
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
