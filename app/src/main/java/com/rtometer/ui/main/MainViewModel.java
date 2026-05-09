package com.rtometer.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private final MutableLiveData<String> message = new MutableLiveData<>("RTOmeter ready");

    @Inject
    public MainViewModel() {}

    public LiveData<String> getMessage() {
        return message;
    }
}
