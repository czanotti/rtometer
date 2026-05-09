package com.rtometer.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rtometer.R;
import com.rtometer.calculator.FiscalQuarterPreset;

import java.time.LocalDate;

public class Step1QuarterFragment extends Fragment implements OnboardingActivity.FragmentStep {

    private RadioGroup rgPreset;
    private Spinner spinnerYear;
    private LinearLayout layoutCustomMonth;
    private NumberPicker pickerMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step1_quarter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rgPreset = view.findViewById(R.id.rgPreset);
        spinnerYear = view.findViewById(R.id.spinnerYear);
        layoutCustomMonth = view.findViewById(R.id.layoutCustomMonth);
        pickerMonth = view.findViewById(R.id.pickerMonth);

        int currentYear = LocalDate.now().getYear();
        String[] years = {
                String.valueOf(currentYear - 1),
                String.valueOf(currentYear),
                String.valueOf(currentYear + 1),
                String.valueOf(currentYear + 2)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapter);
        spinnerYear.setSelection(1);

        pickerMonth.setMinValue(1);
        pickerMonth.setMaxValue(12);
        pickerMonth.setValue(1);

        rgPreset.setOnCheckedChangeListener((group, checkedId) ->
                layoutCustomMonth.setVisibility(
                        checkedId == R.id.rbCustom ? View.VISIBLE : View.GONE));
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void saveToViewModel(OnboardingViewModel vm) {
        int checkedId = rgPreset.getCheckedRadioButtonId();
        FiscalQuarterPreset preset;
        if (checkedId == R.id.rbFebStart) preset = FiscalQuarterPreset.FEB_START;
        else if (checkedId == R.id.rbAprStart) preset = FiscalQuarterPreset.APR_START;
        else if (checkedId == R.id.rbMayStart) preset = FiscalQuarterPreset.MAY_START;
        else if (checkedId == R.id.rbCustom) preset = FiscalQuarterPreset.CUSTOM;
        else preset = FiscalQuarterPreset.CALENDAR;

        vm.setPreset(preset);
        if (preset == FiscalQuarterPreset.CUSTOM) {
            vm.setCustomStartMonth(pickerMonth.getValue());
        }
        vm.setSelectedYear(Integer.parseInt((String) spinnerYear.getSelectedItem()));
    }
}
