package com.rtometer.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rtometer.R;
import com.rtometer.ui.onboarding.OnboardingActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

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
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        TextView textMessage = findViewById(R.id.textMessage);
        viewModel.getMessage().observe(this, textMessage::setText);
    }
}
