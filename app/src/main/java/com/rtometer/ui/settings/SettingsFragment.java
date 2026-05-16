package com.rtometer.ui.settings;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.rtometer.R;
import com.rtometer.calculator.FiscalQuarterPreset;
import com.rtometer.data.db.AppConfig;
import com.rtometer.data.db.Quarter;
import com.rtometer.ui.office.OfficeSetupActivity;

import java.time.LocalTime;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;

    private Button btnWorkStart;
    private Button btnWorkEnd;
    private EditText etGpsInterval;
    private TextView tvTargetValue;
    private SeekBar seekBarTarget;
    private RadioGroup rgPreset;
    private LinearLayout layoutCustomMonth;
    private NumberPicker pickerMonth;
    private Button btnSave;

    private LocalTime workStart = LocalTime.of(9, 30);
    private LocalTime workEnd = LocalTime.of(18, 0);

    private Long currentQuarterId = null;
    private String originalPreset = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        bindViews(view);
        setupPresetListener();
        setupTimeButtons();
        setupSeekBar();

        viewModel.config.observe(getViewLifecycleOwner(), this::populateConfig);
        viewModel.currentQuarter.observe(getViewLifecycleOwner(), this::populateQuarter);

        view.findViewById(R.id.btnManageOffices).setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), OfficeSetupActivity.class)));

        btnSave.setOnClickListener(v -> onSaveTapped());
    }

    private void bindViews(View view) {
        btnWorkStart = view.findViewById(R.id.btnWorkStart);
        btnWorkEnd = view.findViewById(R.id.btnWorkEnd);
        etGpsInterval = view.findViewById(R.id.etGpsInterval);
        tvTargetValue = view.findViewById(R.id.tvTargetValue);
        seekBarTarget = view.findViewById(R.id.seekBarTarget);
        rgPreset = view.findViewById(R.id.rgPreset);
        layoutCustomMonth = view.findViewById(R.id.layoutCustomMonth);
        pickerMonth = view.findViewById(R.id.pickerMonth);
        btnSave = view.findViewById(R.id.btnSave);

        pickerMonth.setMinValue(1);
        pickerMonth.setMaxValue(12);
        pickerMonth.setValue(2);

        seekBarTarget.setMax(100);
        seekBarTarget.setProgress(50);
        tvTargetValue.setText("50%");
    }

    private void setupPresetListener() {
        rgPreset.setOnCheckedChangeListener((group, checkedId) ->
                layoutCustomMonth.setVisibility(
                        checkedId == R.id.rbCustom ? View.VISIBLE : View.GONE));
    }

    private void setupTimeButtons() {
        btnWorkStart.setOnClickListener(v -> new TimePickerDialog(requireContext(),
                (tp, h, m) -> { workStart = LocalTime.of(h, m); updateTimeLabels(); },
                workStart.getHour(), workStart.getMinute(), true).show());

        btnWorkEnd.setOnClickListener(v -> new TimePickerDialog(requireContext(),
                (tp, h, m) -> { workEnd = LocalTime.of(h, m); updateTimeLabels(); },
                workEnd.getHour(), workEnd.getMinute(), true).show());
    }

    private void setupSeekBar() {
        seekBarTarget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                tvTargetValue.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    private void populateConfig(AppConfig cfg) {
        if (cfg == null) return;

        workStart = cfg.workDayStart;
        workEnd = cfg.workDayEnd;
        updateTimeLabels();
        etGpsInterval.setText(String.valueOf(cfg.gpsIntervalMinutes));

        selectPreset(cfg.fiscalQuarterPreset, cfg.customStartMonth);
        originalPreset = cfg.fiscalQuarterPreset;
    }

    private void populateQuarter(Quarter q) {
        if (q == null) return;
        currentQuarterId = q.id;
        int pct = Math.round(q.targetPercentage * 100);
        seekBarTarget.setProgress(pct);
        tvTargetValue.setText(pct + "%");
    }

    private void selectPreset(String presetName, int customMonth) {
        if (presetName == null) {
            rgPreset.check(R.id.rbFebStart);
            return;
        }
        switch (presetName) {
            case "CALENDAR": rgPreset.check(R.id.rbCalendar); break;
            case "APR_START": rgPreset.check(R.id.rbAprStart); break;
            case "MAY_START": rgPreset.check(R.id.rbMayStart); break;
            case "CUSTOM":
                rgPreset.check(R.id.rbCustom);
                pickerMonth.setValue(customMonth);
                break;
            default: rgPreset.check(R.id.rbFebStart); break;
        }
    }

    private void updateTimeLabels() {
        btnWorkStart.setText(String.format(Locale.getDefault(), "%02d:%02d",
                workStart.getHour(), workStart.getMinute()));
        btnWorkEnd.setText(String.format(Locale.getDefault(), "%02d:%02d",
                workEnd.getHour(), workEnd.getMinute()));
    }

    private FiscalQuarterPreset selectedPreset() {
        int id = rgPreset.getCheckedRadioButtonId();
        if (id == R.id.rbCalendar) return FiscalQuarterPreset.CALENDAR;
        if (id == R.id.rbAprStart) return FiscalQuarterPreset.APR_START;
        if (id == R.id.rbMayStart) return FiscalQuarterPreset.MAY_START;
        if (id == R.id.rbCustom) return FiscalQuarterPreset.CUSTOM;
        return FiscalQuarterPreset.FEB_START;
    }

    private void onSaveTapped() {
        String gpsText = etGpsInterval.getText().toString().trim();
        if (gpsText.isEmpty() || Integer.parseInt(gpsText) <= 0) {
            etGpsInterval.setError(getString(R.string.settings_gps_error));
            etGpsInterval.requestFocus();
            return;
        }
        etGpsInterval.setError(null);

        FiscalQuarterPreset preset = selectedPreset();
        int customMonth = pickerMonth.getValue();
        float target = seekBarTarget.getProgress() / 100f;

        boolean noQuarters = currentQuarterId == null;
        boolean presetChanged = !preset.name().equals(originalPreset);

        if (noQuarters) {
            viewModel.resetQuarters(preset, customMonth, target);
            viewModel.saveConfig(workStart, workEnd, Integer.parseInt(gpsText), preset, customMonth);
            navigateToDashboard();
        } else if (presetChanged) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.settings_reset_title)
                    .setMessage(R.string.settings_reset_message)
                    .setPositiveButton(R.string.settings_reset_confirm, (d, w) -> {
                        viewModel.resetQuarters(preset, customMonth, target);
                        viewModel.saveConfig(workStart, workEnd, Integer.parseInt(gpsText),
                                preset, customMonth);
                        navigateToDashboard();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else {
            viewModel.saveConfig(workStart, workEnd, Integer.parseInt(gpsText), preset, customMonth);
            viewModel.saveQuarterTarget(currentQuarterId, target);
            showSavedAndNavigate();
        }
    }

    private void showSavedAndNavigate() {
        Snackbar.make(requireView(), R.string.settings_saved, Snackbar.LENGTH_SHORT).show();
        navigateToDashboard();
    }

    private void navigateToDashboard() {
        BottomNavigationView nav = requireActivity().findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_dashboard);
    }
}
