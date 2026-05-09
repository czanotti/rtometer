package com.rtometer.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.rtometer.calculator.QuarterStats;
import com.rtometer.dashboard.DashboardRepository;
import com.rtometer.data.db.Quarter;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private final DashboardRepository repository;

    @Inject
    public MainViewModel(DashboardRepository repository) {
        this.repository = repository;
    }

    public LiveData<Quarter> getCurrentQuarter() {
        return repository.getCurrentQuarter();
    }

    public LiveData<QuarterStats> getStats() {
        return repository.getStats();
    }
}
