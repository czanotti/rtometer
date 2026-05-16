package com.rtometer.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rtometer.R;
import com.rtometer.calculator.FiscalQuarterPreset;

public class Step1QuarterFragment extends Fragment implements OnboardingActivity.FragmentStep {

    private RadioGroup rgPreset;
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
        layoutCustomMonth = view.findViewById(R.id.layoutCustomMonth);
        pickerMonth = view.findViewById(R.id.pickerMonth);

        pickerMonth.setMinValue(1);
        pickerMonth.setMaxValue(12);
        pickerMonth.setValue(2);

        rgPreset.check(R.id.rbFebStart);

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
        if (checkedId == R.id.rbCalendar) preset = FiscalQuarterPreset.CALENDAR;
        else if (checkedId == R.id.rbAprStart) preset = FiscalQuarterPreset.APR_START;
        else if (checkedId == R.id.rbMayStart) preset = FiscalQuarterPreset.MAY_START;
        else if (checkedId == R.id.rbCustom) preset = FiscalQuarterPreset.CUSTOM;
        else preset = FiscalQuarterPreset.FEB_START;

        vm.setPreset(preset);
        if (preset == FiscalQuarterPreset.CUSTOM) {
            vm.setCustomStartMonth(pickerMonth.getValue());
        }
    }
}
