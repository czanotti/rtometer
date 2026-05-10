package com.rtometer.ui.office;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rtometer.data.db.Office;
import com.rtometer.data.db.OfficeDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OfficeSetupViewModel extends ViewModel {

    final OfficeDao officeDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Office>> officesLive = new MutableLiveData<>(new java.util.ArrayList<>());
    public final LiveData<List<Office>> offices = officesLive;

    @Inject
    public OfficeSetupViewModel(OfficeDao officeDao) {
        this.officeDao = officeDao;
        executor.execute(this::reload);
    }

    public void addOffice(String name, double lat, double lon, int radiusMeters) {
        executor.execute(() -> {
            insertOffice(name, lat, lon, radiusMeters);
            reload();
        });
    }

    public void updateOffice(Office office) {
        executor.execute(() -> {
            officeDao.update(office);
            reload();
        });
    }

    public boolean deleteOffice(Office office) {
        List<Office> current = officesLive.getValue();
        if (current == null || current.size() <= 1) return false;
        executor.execute(() -> {
            officeDao.delete(office);
            reload();
        });
        return true;
    }

    @Override
    protected void onCleared() {
        executor.shutdown();
        try { executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS); }
        catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    private void insertOffice(String name, double lat, double lon, int radiusMeters) {
        Office o = new Office();
        o.name = name;
        o.latitude = lat;
        o.longitude = lon;
        o.radiusMeters = radiusMeters;
        List<Office> existing = officeDao.getAll();
        o.isPrimary = existing.isEmpty();
        officeDao.insert(o);
    }

    private void reload() {
        officesLive.postValue(officeDao.getAll());
    }

    // Synchronous variants for unit tests
    void addOfficeSync(String name, double lat, double lon, int radiusMeters) {
        insertOffice(name, lat, lon, radiusMeters);
        officesLive.postValue(officeDao.getAll());
    }

    void updateOfficeSync(Office office) {
        officeDao.update(office);
        officesLive.postValue(officeDao.getAll());
    }

    boolean deleteOfficeSync(Office office) {
        List<Office> current = officeDao.getAll();
        if (current.size() <= 1) return false;
        officeDao.delete(office);
        officesLive.postValue(officeDao.getAll());
        return true;
    }
}
