package com.rtometer.ui.settings;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rtometer.calculator.FiscalQuarterFactory;
import com.rtometer.calculator.FiscalQuarterPreset;
import com.rtometer.data.db.AppConfig;
import com.rtometer.data.db.AppConfigDao;
import com.rtometer.data.db.BankHolidayDao;
import com.rtometer.data.db.Quarter;
import com.rtometer.data.db.QuarterDao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SettingsViewModel extends ViewModel {

    private final AppConfigDao configDao;
    private final QuarterDao quarterDao;
    final BankHolidayDao bankHolidayDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    final MutableLiveData<AppConfig> config = new MutableLiveData<>();
    final MutableLiveData<Quarter> currentQuarter = new MutableLiveData<>();

    @Inject
    public SettingsViewModel(AppConfigDao configDao, QuarterDao quarterDao,
                             BankHolidayDao bankHolidayDao) {
        this.configDao = configDao;
        this.quarterDao = quarterDao;
        this.bankHolidayDao = bankHolidayDao;
        executor.submit(this::loadSync);
    }

    void loadSync() {
        AppConfig cfg = configDao.get();
        if (cfg == null) {
            cfg = new AppConfig();
            cfg.id = 1;
        }
        config.postValue(cfg);
        currentQuarter.postValue(quarterDao.getByDate(LocalDate.now().toString()));
    }

    public void saveConfig(LocalTime start, LocalTime end, int gps,
                           FiscalQuarterPreset preset, int customMonth) {
        executor.submit(() -> saveConfigSync(start, end, gps, preset, customMonth));
    }

    void saveConfigSync(LocalTime start, LocalTime end, int gps,
                        FiscalQuarterPreset preset, int customMonth) {
        AppConfig cfg = configDao.get();
        if (cfg == null) {
            cfg = new AppConfig();
            cfg.id = 1;
        }
        cfg.workDayStart = start;
        cfg.workDayEnd = end;
        cfg.gpsIntervalMinutes = gps;
        cfg.fiscalQuarterPreset = preset.name();
        cfg.customStartMonth = (preset == FiscalQuarterPreset.CUSTOM) ? customMonth : 2;
        configDao.upsert(cfg);
        config.postValue(cfg);
    }

    public void saveQuarterTarget(long quarterId, float target) {
        executor.submit(() -> saveQuarterTargetSync(quarterId, target));
    }

    void saveQuarterTargetSync(long quarterId, float target) {
        Quarter q = quarterDao.getById(quarterId);
        if (q == null) return;
        q.targetPercentage = target;
        quarterDao.update(q);
        currentQuarter.postValue(q);
    }

    public void resetQuarters(FiscalQuarterPreset preset, int customMonth, float target) {
        executor.submit(() -> resetQuartersSync(preset, customMonth, target));
    }

    void resetQuartersSync(FiscalQuarterPreset preset, int customMonth, float target) {
        quarterDao.deleteAll();

        List<Quarter> quarters;
        if (preset == FiscalQuarterPreset.CUSTOM) {
            quarters = FiscalQuarterFactory.createCustom(customMonth);
        } else {
            quarters = FiscalQuarterFactory.create(preset);
        }
        for (Quarter q : quarters) {
            q.targetPercentage = target;
            quarterDao.insert(q);
        }

        AppConfig cfg = configDao.get();
        if (cfg == null) {
            cfg = new AppConfig();
            cfg.id = 1;
        }
        cfg.fiscalQuarterPreset = preset.name();
        cfg.customStartMonth = (preset == FiscalQuarterPreset.CUSTOM) ? customMonth : 2;
        configDao.upsert(cfg);
        config.postValue(cfg);
        currentQuarter.postValue(quarterDao.getByDate(LocalDate.now().toString()));
    }

    @Override
    protected void onCleared() {
        executor.shutdown();
        try { executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS); }
        catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
