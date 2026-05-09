package com.rtometer.ui.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rtometer.history.HistoryRepository;
import com.rtometer.history.PastQuarterEntry;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HistoryViewModel extends ViewModel {

    private final HistoryRepository repository;
    private final MutableLiveData<List<PastQuarterEntry>> pastQuarters = new MutableLiveData<>();

    @Inject
    public HistoryViewModel(HistoryRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<PastQuarterEntry>> getPastQuarters() {
        return pastQuarters;
    }

    public void load() {
        Executors.newSingleThreadExecutor().execute(() ->
                pastQuarters.postValue(repository.loadPastQuarters()));
    }
}
