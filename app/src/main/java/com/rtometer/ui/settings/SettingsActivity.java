package com.rtometer.ui.settings;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.rtometer.ui.SecureActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import com.rtometer.R;
import com.rtometer.calculator.FiscalQuarterPreset;
import com.rtometer.data.db.AppConfig;
import com.rtometer.data.db.Quarter;
import com.rtometer.ui.office.OfficeSetupActivity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends SecureActivity {

    private SettingsViewModel viewModel;

    private Button btnWorkStart;
    private Button btnWorkEnd;
    private EditText etGpsInterval;
    private TextView tvTargetValue;
    private SeekBar seekBarTarget;
    private RadioGroup rgPreset;
    private LinearLayout layoutCustomMonth;
    private NumberPicker pickerMonth;
    private Spinner spinnerYear;
    private RadioGroup rgCountry;
    private Button btnSave;

    private LocalTime workStart = LocalTime.of(8, 0);
    private LocalTime workEnd = LocalTime.of(18, 0);

    private Long currentQuarterId = null;
    private String originalPreset = null;
    private int originalYear = LocalDate.now().getYear();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        bindViews();
        setupYearSpinner();
        setupPresetListener();
        setupTimeButtons();
        setupSeekBar();

        viewModel.config.observe(this, this::populateConfig);
        viewModel.currentQuarter.observe(this, this::populateQuarter);

        findViewById(R.id.btnManageOffices).setOnClickListener(v ->
                startActivity(new Intent(this, OfficeSetupActivity.class)));

        btnSave.setOnClickListener(v -> onSaveTapped());
    }

    private void bindViews() {
        btnWorkStart = findViewById(R.id.btnWorkStart);
        btnWorkEnd = findViewById(R.id.btnWorkEnd);
        etGpsInterval = findViewById(R.id.etGpsInterval);
        tvTargetValue = findViewById(R.id.tvTargetValue);
        seekBarTarget = findViewById(R.id.seekBarTarget);
        rgPreset = findViewById(R.id.rgPreset);
        layoutCustomMonth = findViewById(R.id.layoutCustomMonth);
        pickerMonth = findViewById(R.id.pickerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        rgCountry = findViewById(R.id.rgCountry);
        btnSave = findViewById(R.id.btnSave);

        pickerMonth.setMinValue(1);
        pickerMonth.setMaxValue(12);
        pickerMonth.setValue(1);

        seekBarTarget.setMax(100);
        seekBarTarget.setProgress(60);
        tvTargetValue.setText("60%");
    }

    private void setupYearSpinner() {
        int current = LocalDate.now().getYear();
        String[] years = {
                String.valueOf(current - 1),
                String.valueOf(current),
                String.valueOf(current + 1),
                String.valueOf(current + 2)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapter);
        spinnerYear.setSelection(1);
    }

    private void setupPresetListener() {
        rgPreset.setOnCheckedChangeListener((group, checkedId) ->
                layoutCustomMonth.setVisibility(
                        checkedId == R.id.rbCustom ? android.view.View.VISIBLE : android.view.View.GONE));
    }

    private void setupTimeButtons() {
        btnWorkStart.setOnClickListener(v -> new TimePickerDialog(this,
                (tp, h, m) -> { workStart = LocalTime.of(h, m); updateTimeLabels(); },
                workStart.getHour(), workStart.getMinute(), true).show());

        btnWorkEnd.setOnClickListener(v -> new TimePickerDialog(this,
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

        selectCountry(cfg.bankHolidayCountry);
    }

    private void populateQuarter(Quarter q) {
        if (q == null) return;
        currentQuarterId = q.id;
        int pct = Math.round(q.targetPercentage * 100);
        seekBarTarget.setProgress(pct);
        tvTargetValue.setText(pct + "%");

        int year = q.startDate.getYear();
        originalYear = year;
        selectYear(year);
    }

    private void selectPreset(String presetName, int customMonth) {
        if (presetName == null) {
            rgPreset.check(R.id.rbCalendar);
            return;
        }
        switch (presetName) {
            case "FEB_START": rgPreset.check(R.id.rbFebStart); break;
            case "APR_START": rgPreset.check(R.id.rbAprStart); break;
            case "MAY_START": rgPreset.check(R.id.rbMayStart); break;
            case "CUSTOM":
                rgPreset.check(R.id.rbCustom);
                pickerMonth.setValue(customMonth);
                break;
            default: rgPreset.check(R.id.rbCalendar); break;
        }
    }

    private void selectYear(int year) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerYear.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (String.valueOf(year).equals(adapter.getItem(i))) {
                spinnerYear.setSelection(i);
                return;
            }
        }
    }

    private void selectCountry(String country) {
        if (country == null) { rgCountry.check(R.id.rbCountryNone); return; }
        switch (country) {
            case "IT": rgCountry.check(R.id.rbIT); break;
            case "GB": rgCountry.check(R.id.rbGB); break;
            case "US": rgCountry.check(R.id.rbUS); break;
            case "DE": rgCountry.check(R.id.rbDE); break;
            case "FR": rgCountry.check(R.id.rbFR); break;
            default: rgCountry.check(R.id.rbCountryNone); break;
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
        if (id == R.id.rbFebStart) return FiscalQuarterPreset.FEB_START;
        if (id == R.id.rbAprStart) return FiscalQuarterPreset.APR_START;
        if (id == R.id.rbMayStart) return FiscalQuarterPreset.MAY_START;
        if (id == R.id.rbCustom) return FiscalQuarterPreset.CUSTOM;
        return FiscalQuarterPreset.CALENDAR;
    }

    private String selectedCountry() {
        int id = rgCountry.getCheckedRadioButtonId();
        if (id == R.id.rbIT) return "IT";
        if (id == R.id.rbGB) return "GB";
        if (id == R.id.rbUS) return "US";
        if (id == R.id.rbDE) return "DE";
        if (id == R.id.rbFR) return "FR";
        return null;
    }

    private int selectedYear() {
        return Integer.parseInt((String) spinnerYear.getSelectedItem());
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
        int year = selectedYear();
        float target = seekBarTarget.getProgress() / 100f;
        String country = selectedCountry();

        boolean noQuarters = currentQuarterId == null;
        boolean presetChanged = !preset.name().equals(originalPreset);
        boolean yearChanged = year != originalYear;

        if (noQuarters) {
            viewModel.resetQuarters(preset, customMonth, year, target);
            viewModel.saveConfig(workStart, workEnd, Integer.parseInt(gpsText),
                    country, preset, customMonth);
            finish();
        } else if (presetChanged || yearChanged) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.settings_reset_title)
                    .setMessage(R.string.settings_reset_message)
                    .setPositiveButton(R.string.settings_reset_confirm, (d, w) -> {
                        viewModel.resetQuarters(preset, customMonth, year, target);
                        viewModel.saveConfig(workStart, workEnd, Integer.parseInt(gpsText),
                                country, preset, customMonth);
                        finish();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else {
            viewModel.saveConfig(workStart, workEnd, Integer.parseInt(gpsText),
                    country, preset, customMonth);
            viewModel.saveQuarterTarget(currentQuarterId, target);
            showSavedAndFinish();
        }
    }

    private void showSavedAndFinish() {
        Snackbar.make(findViewById(android.R.id.content),
                        R.string.settings_saved, Snackbar.LENGTH_SHORT)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar sb, int event) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
