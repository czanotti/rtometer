package com.rtometer.ui.onboarding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rtometer.calculator.FiscalQuarterFactory;
import com.rtometer.calculator.FiscalQuarterPreset;
import com.rtometer.data.db.AppConfig;
import com.rtometer.data.db.AppConfigDao;
import com.rtometer.data.db.Office;
import com.rtometer.data.db.OfficeDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.QuarterDao;
import com.rtometer.data.model.BankHolidayCountry;

import java.time.LocalDate;
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
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private FiscalQuarterPreset preset = FiscalQuarterPreset.CALENDAR;
    private int customStartMonth = 1;
    private int selectedYear = LocalDate.now().getYear();
    private int targetPercentage = 60;
    private LocalTime workDayStart = LocalTime.of(8, 0);
    private LocalTime workDayEnd = LocalTime.of(18, 0);
    private int gpsIntervalMinutes = 15;
    private String officeName = "";
    private double officeLat = 0;
    private double officeLng = 0;
    private int officeRadiusMeters = 200;
    private BankHolidayCountry bankHolidayCountry = null;
    private int preloadCount = 0;

    private final MutableLiveData<Boolean> finished = new MutableLiveData<>();

    @Inject
    public OnboardingViewModel(QuarterDao quarterDao, OfficeDao officeDao, AppConfigDao configDao) {
        this.quarterDao = quarterDao;
        this.officeDao = officeDao;
        this.configDao = configDao;
    }

    public void setPreset(FiscalQuarterPreset preset) { this.preset = preset; }
    public void setCustomStartMonth(int month) { this.customStartMonth = month; }
    public void setSelectedYear(int year) { this.selectedYear = year; }
    public void setTargetPercentage(int pct) { this.targetPercentage = pct; }
    public void setWorkDayStart(LocalTime t) { this.workDayStart = t; }
    public void setWorkDayEnd(LocalTime t) { this.workDayEnd = t; }
    public void setGpsIntervalMinutes(int mins) { this.gpsIntervalMinutes = mins; }
    public void setOfficeName(String name) { this.officeName = name; }
    public void setOfficeLat(double lat) { this.officeLat = lat; }
    public void setOfficeLng(double lng) { this.officeLng = lng; }
    public void setOfficeRadiusMeters(int r) { this.officeRadiusMeters = r; }
    public void setBankHolidayCountry(BankHolidayCountry country) { this.bankHolidayCountry = country; }
    public void setPreloadCount(int count) { this.preloadCount = count; }

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
            quarters = FiscalQuarterFactory.createCustom(customStartMonth, selectedYear, 0);
        } else {
            quarters = FiscalQuarterFactory.create(preset, selectedYear, 0);
        }

        LocalDate today = LocalDate.now();
        for (Quarter q : quarters) {
            q.targetPercentage = targetPercentage / 100f;
            long id = quarterDao.insert(q);
            if (preloadCount > 0 && !today.isBefore(q.startDate) && !today.isAfter(q.endDate)) {
                q.id = id;
                q.preloadCount = preloadCount;
                quarterDao.update(q);
            }
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
        config.bankHolidayCountry = bankHolidayCountry != null ? bankHolidayCountry.name() : null;
        config.fiscalYearOffset = 0;
        config.fiscalQuarterPreset = preset.name();
        config.customStartMonth = (preset == FiscalQuarterPreset.CUSTOM) ? customStartMonth : 1;
        configDao.upsert(config);
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}
