package com.rtometer.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.rtometer.ui.SecureActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rtometer.R;
import com.rtometer.gps.LocationPermissionChecker;
import com.rtometer.ui.calendar.CalendarFragment;
import com.rtometer.ui.dashboard.DashboardFragment;
import com.rtometer.ui.history.HistoryFragment;
import com.rtometer.ui.office.OfficeSetupActivity;
import com.rtometer.ui.onboarding.OnboardingActivity;
import com.rtometer.ui.settings.SettingsFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends SecureActivity {

    private LinearLayout gpsBanner;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        gpsBanner = findViewById(R.id.gpsBanner);
        TextView settingsLink = findViewById(R.id.gpsBannerSettings);
        settingsLink.setOnClickListener(v -> openLocationSettings());

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
            } else if (id == R.id.nav_settings) {
                showFragment(new SettingsFragment());
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gpsBanner != null) {
            updateGpsBanner();
        }
    }

    private void updateGpsBanner() {
        boolean denied = LocationPermissionChecker.isDenied(this);
        if (denied && LocationPermissionChecker.hasBackgroundLocation(this)) {
            LocationPermissionChecker.setDenied(this, false);
            denied = false;
        }
        gpsBanner.setVisibility(denied ? View.VISIBLE : View.GONE);
    }

    private void openLocationSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
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
        return super.onOptionsItemSelected(item);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
