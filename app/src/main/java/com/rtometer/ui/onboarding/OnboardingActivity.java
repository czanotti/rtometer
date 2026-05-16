package com.rtometer.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.rtometer.ui.SecureActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rtometer.R;
import com.rtometer.ui.main.MainActivity;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OnboardingActivity extends SecureActivity {

    interface FragmentStep {
        boolean isValid();
        void saveToViewModel(OnboardingViewModel vm);
    }

    private OnboardingViewModel vm;
    private List<FragmentStep> steps;
    private int currentStep = 0;

    private ProgressBar progressBar;
    private TextView stepLabel;
    private Button btnBack;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        vm = new ViewModelProvider(this).get(OnboardingViewModel.class);

        progressBar = findViewById(R.id.progressBar);
        stepLabel = findViewById(R.id.stepLabel);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        steps = Arrays.asList(
                new Step1QuarterFragment(),
                new Step2TargetFragment(),
                new Step3HoursFragment(),
                new Step4OfficeFragment()
        );

        if (savedInstanceState == null) {
            showStep(0);
        } else {
            currentStep = savedInstanceState.getInt("currentStep", 0);
            updateNavUi();
        }

        btnBack.setOnClickListener(v -> {
            if (currentStep > 0) showStep(currentStep - 1);
        });

        btnNext.setOnClickListener(v -> {
            FragmentStep step = (FragmentStep) getSupportFragmentManager()
                    .findFragmentById(R.id.container);
            if (step == null || !step.isValid()) return;
            step.saveToViewModel(vm);
            if (currentStep == steps.size() - 1) {
                btnNext.setEnabled(false);
                vm.finish();
            } else {
                showStep(currentStep + 1);
            }
        });

        vm.getFinished().observe(this, done -> {
            if (Boolean.TRUE.equals(done)) {
                getSharedPreferences("rtometer", MODE_PRIVATE)
                        .edit()
                        .putBoolean("onboarding_complete", true)
                        .apply();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentStep", currentStep);
    }

    private void showStep(int index) {
        currentStep = index;
        Fragment frag = (Fragment) steps.get(index);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, frag)
                .commit();
        updateNavUi();
    }

    private void updateNavUi() {
        progressBar.setProgress(currentStep + 1);
        stepLabel.setText(getString(R.string.step_of, currentStep + 1, steps.size()));
        btnBack.setVisibility(currentStep > 0 ? View.VISIBLE : View.INVISIBLE);
        boolean isLastStep = currentStep == steps.size() - 1;
        btnNext.setText(isLastStep ? R.string.btn_finish : R.string.btn_next);
        btnNext.setEnabled(true);
    }
}
