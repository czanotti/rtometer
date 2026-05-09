package com.rtometer.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rtometer.R;

public class Step2TargetFragment extends Fragment implements OnboardingActivity.FragmentStep {

    private SeekBar seekBar;
    private TextView tvValue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step2_target, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        seekBar = view.findViewById(R.id.seekBarTarget);
        tvValue = view.findViewById(R.id.tvTargetValue);
        seekBar.setMax(100);
        seekBar.setProgress(60);
        tvValue.setText("60%");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                tvValue.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    @Override
    public boolean isValid() { return true; }

    @Override
    public void saveToViewModel(OnboardingViewModel vm) {
        vm.setTargetPercentage(seekBar.getProgress());
    }
}
