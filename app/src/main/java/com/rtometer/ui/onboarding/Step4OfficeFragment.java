package com.rtometer.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rtometer.R;

public class Step4OfficeFragment extends Fragment implements OnboardingActivity.FragmentStep {

    private EditText etName;
    private EditText etLat;
    private EditText etLng;
    private EditText etRadius;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step4_office, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etName = view.findViewById(R.id.etOfficeName);
        etLat = view.findViewById(R.id.etLat);
        etLng = view.findViewById(R.id.etLng);
        etRadius = view.findViewById(R.id.etRadius);

        etName.setText("Kings Building");
        etLat.setText("53.3478");
        etLng.setText("-6.27591");
        etRadius.setText("200");
    }

    @Override
    public boolean isValid() {
        if (etName.getText().toString().trim().isEmpty()) return false;
        if (!isDouble(etLat.getText().toString())) return false;
        if (!isDouble(etLng.getText().toString())) return false;
        try {
            return Integer.parseInt(etRadius.getText().toString().trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDouble(String s) {
        try {
            Double.parseDouble(s.trim());
            return !s.trim().isEmpty();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void saveToViewModel(OnboardingViewModel vm) {
        vm.setOfficeName(etName.getText().toString().trim());
        vm.setOfficeLat(Double.parseDouble(etLat.getText().toString().trim()));
        vm.setOfficeLng(Double.parseDouble(etLng.getText().toString().trim()));
        vm.setOfficeRadiusMeters(Integer.parseInt(etRadius.getText().toString().trim()));
    }
}
