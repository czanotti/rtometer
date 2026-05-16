package com.rtometer.ui.onboarding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rtometer.calculator.FiscalQuarterFactory;
import com.rtometer.calculator.FiscalQuarterPreset;
import com.rtometer.data.db.AppConfig;
import com.rtometer.data.db.AppConfigDao;
import com.rtometer.data.db.BankHolidayDao;
import com.rtometer.data.db.Office;
import com.rtometer.data.db.OfficeDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.QuarterDao;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OnboardingViewModel extends ViewModel {

    private final QuarterDao quarterDao;
    private final OfficeDao officeDao;
    private final AppConfigDao configDao;
    final BankHolidayDao bankHolidayDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private FiscalQuarterPreset preset = FiscalQuarterPreset.FEB_START;
    private int customStartMonth = 2;
    private int targetPercentage = 50;
    private LocalTime workDayStart = LocalTime.of(9, 30);
    private LocalTime workDayEnd = LocalTime.of(18, 0);
    private int gpsIntervalMinutes = 120;
    private String officeName = "";
    private double officeLat = 0;
    private double officeLng = 0;
    private int officeRadiusMeters = 200;

    private final MutableLiveData<Boolean> finished = new MutableLiveData<>();

    @Inject
    public OnboardingViewModel(QuarterDao quarterDao, OfficeDao officeDao, AppConfigDao configDao,
                               BankHolidayDao bankHolidayDao) {
        this.quarterDao = quarterDao;
        this.officeDao = officeDao;
        this.configDao = configDao;
        this.bankHolidayDao = bankHolidayDao;
    }

    public void setPreset(FiscalQuarterPreset preset) { this.preset = preset; }
    public void setCustomStartMonth(int month) { this.customStartMonth = month; }
    public void setTargetPercentage(int pct) { this.targetPercentage = pct; }
    public void setWorkDayStart(LocalTime t) { this.workDayStart = t; }
    public void setWorkDayEnd(LocalTime t) { this.workDayEnd = t; }
    public void setGpsIntervalMinutes(int mins) { this.gpsIntervalMinutes = mins; }
    public void setOfficeName(String name) { this.officeName = name; }
    public void setOfficeLat(double lat) { this.officeLat = lat; }
    public void setOfficeLng(double lng) { this.officeLng = lng; }
    public void setOfficeRadiusMeters(int r) { this.officeRadiusMeters = r; }

    public LiveData<Boolean> getFinished() { return finished; }

    public void finish() {
        executor.submit(() -> {
            persistSync();
            finished.postValue(true);
        });
    }

    void persistSync() {
        List<Quarter> quarters;
        if (preset == FiscalQuarterPreset.CUSTOM) {
            quarters = FiscalQuarterFactory.createCustom(customStartMonth);
        } else {
            quarters = FiscalQuarterFactory.create(preset);
        }

        for (Quarter q : quarters) {
            q.targetPercentage = targetPercentage / 100f;
            quarterDao.insert(q);
        }

        Office office = new Office();
        office.name = officeName;
        office.latitude = officeLat;
        office.longitude = officeLng;
        office.radiusMeters = officeRadiusMeters;
        office.isPrimary = true;
        officeDao.insert(office);

        AppConfig config = new AppConfig();
        config.id = 1;
        config.workDayStart = workDayStart;
        config.workDayEnd = workDayEnd;
        config.gpsIntervalMinutes = gpsIntervalMinutes;
        config.fiscalQuarterPreset = preset.name();
        config.customStartMonth = (preset == FiscalQuarterPreset.CUSTOM) ? customStartMonth : 2;
        configDao.upsert(config);
    }

    @Override
    protected void onCleared() {
        executor.shutdown();
        try { executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS); }
        catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
