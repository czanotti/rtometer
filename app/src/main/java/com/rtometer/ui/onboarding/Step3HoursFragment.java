package com.rtometer.ui.onboarding;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rtometer.R;

import java.time.LocalTime;
import java.util.Locale;

public class Step3HoursFragment extends Fragment implements OnboardingActivity.FragmentStep {

    private Button btnStart;
    private Button btnEnd;
    private EditText etGpsInterval;

    private LocalTime workStart = LocalTime.of(8, 0);
    private LocalTime workEnd = LocalTime.of(18, 0);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step3_hours, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnStart = view.findViewById(R.id.btnWorkStart);
        btnEnd = view.findViewById(R.id.btnWorkEnd);
        etGpsInterval = view.findViewById(R.id.etGpsInterval);
        etGpsInterval.setText("15");
        updateLabels();

        btnStart.setOnClickListener(v -> new TimePickerDialog(requireContext(),
                (tp, h, m) -> { workStart = LocalTime.of(h, m); updateLabels(); },
                workStart.getHour(), workStart.getMinute(), true).show());

        btnEnd.setOnClickListener(v -> new TimePickerDialog(requireContext(),
                (tp, h, m) -> { workEnd = LocalTime.of(h, m); updateLabels(); },
                workEnd.getHour(), workEnd.getMinute(), true).show());
    }

    private void updateLabels() {
        btnStart.setText(String.format(Locale.getDefault(), "%02d:%02d",
                workStart.getHour(), workStart.getMinute()));
        btnEnd.setText(String.format(Locale.getDefault(), "%02d:%02d",
                workEnd.getHour(), workEnd.getMinute()));
    }

    @Override
    public boolean isValid() {
        String txt = etGpsInterval.getText().toString().trim();
        if (txt.isEmpty()) return false;
        try {
            return Integer.parseInt(txt) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void saveToViewModel(OnboardingViewModel vm) {
        vm.setWorkDayStart(workStart);
        vm.setWorkDayEnd(workEnd);
        vm.setGpsIntervalMinutes(Integer.parseInt(etGpsInterval.getText().toString().trim()));
    }
}
