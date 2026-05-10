package com.rtometer.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rtometer.R;
import com.rtometer.ui.calendar.CalendarFragment;
import com.rtometer.ui.dashboard.DashboardFragment;
import com.rtometer.ui.history.HistoryFragment;
import com.rtometer.ui.office.OfficeSetupActivity;
import com.rtometer.ui.onboarding.OnboardingActivity;
import com.rtometer.ui.settings.SettingsActivity;

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

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        if (savedInstanceState == null) {
            showFragment(new DashboardFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                showFragment(new DashboardFragment());
            } else if (id == R.id.nav_calendar) {
                showFragment(new CalendarFragment());
            } else if (id == R.id.nav_history) {
                showFragment(new HistoryFragment());
            }
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_manage_offices) {
            startActivity(new Intent(this, OfficeSetupActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
