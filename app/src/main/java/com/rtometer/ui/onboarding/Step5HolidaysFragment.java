package com.rtometer.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rtometer.R;
import com.rtometer.data.model.BankHolidayCountry;

public class Step5HolidaysFragment extends Fragment implements OnboardingActivity.FragmentStep {

    private RadioGroup rgCountry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step5_holidays, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rgCountry = view.findViewById(R.id.rgCountry);
    }

    @Override
    public boolean isValid() { return true; }

    @Override
    public void saveToViewModel(OnboardingViewModel vm) {
        int checkedId = rgCountry.getCheckedRadioButtonId();
        BankHolidayCountry country = null;
        if (checkedId == R.id.rbIT) country = BankHolidayCountry.IT;
        else if (checkedId == R.id.rbGB) country = BankHolidayCountry.GB;
        else if (checkedId == R.id.rbUS) country = BankHolidayCountry.US;
        else if (checkedId == R.id.rbDE) country = BankHolidayCountry.DE;
        else if (checkedId == R.id.rbFR) country = BankHolidayCountry.FR;
        vm.setBankHolidayCountry(country);
    }
}
