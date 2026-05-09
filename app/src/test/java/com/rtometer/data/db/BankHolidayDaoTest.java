package com.rtometer.data.db;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rtometer.data.model.BankHolidayCountry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class BankHolidayDaoTest {

    private AppDatabase db;
    private BankHolidayDao dao;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.bankHolidayDao();
    }

    @After
    public void teardown() {
        db.close();
    }

    @Test
    public void getByYear_emptyWhenNoneInserted() {
        assertTrue(dao.getByYear(2025).isEmpty());
    }

    @Test
    public void insert_thenGetByYear_returnsHoliday() {
        BankHoliday h = holiday(LocalDate.of(2025, 12, 25), "Christmas Day", "GB", 2025);
        dao.insert(h);

        List<BankHoliday> result = dao.getByYear(2025);
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2025, 12, 25), result.get(0).date);
        assertEquals("Christmas Day", result.get(0).name);
        assertEquals("GB", result.get(0).countryCode);
    }

    @Test
    public void insertAll_thenGetByYear_returnsAll() {
        BankHoliday h1 = holiday(LocalDate.of(2025, 1, 1), "New Year", "IT", 2025);
        BankHoliday h2 = holiday(LocalDate.of(2025, 4, 25), "Liberation Day", "IT", 2025);
        BankHoliday h3 = holiday(LocalDate.of(2025, 12, 25), "Christmas", "IT", 2025);
        dao.insertAll(List.of(h1, h2, h3));

        assertEquals(3, dao.getByYear(2025).size());
    }

    @Test
    public void getByYear_doesNotReturnOtherYears() {
        BankHoliday h2025 = holiday(LocalDate.of(2025, 1, 1), "New Year", "IT", 2025);
        BankHoliday h2024 = holiday(LocalDate.of(2024, 1, 1), "New Year", "IT", 2024);
        dao.insert(h2025);
        dao.insert(h2024);

        assertEquals(1, dao.getByYear(2025).size());
        assertEquals(1, dao.getByYear(2024).size());
    }

    @Test
    public void delete_removesHoliday() {
        BankHoliday h = holiday(LocalDate.of(2025, 12, 25), "Christmas", "GB", 2025);
        long id = dao.insert(h);
        h.id = id;

        dao.delete(h);

        assertTrue(dao.getByYear(2025).isEmpty());
    }

    @Test
    public void deleteById_removesCorrectHoliday() {
        BankHoliday h1 = holiday(LocalDate.of(2025, 1, 1), "New Year", "IT", 2025);
        BankHoliday h2 = holiday(LocalDate.of(2025, 12, 25), "Christmas", "IT", 2025);
        long id1 = dao.insert(h1);
        dao.insert(h2);

        dao.deleteById(id1);

        List<BankHoliday> result = dao.getByYear(2025);
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2025, 12, 25), result.get(0).date);
    }

    @Test
    public void deleteByCountryCode_removesOnlyThatCountry() {
        dao.insert(holiday(LocalDate.of(2025, 1, 1), "New Year", "IT", 2025));
        dao.insert(holiday(LocalDate.of(2025, 12, 25), "Christmas", "IT", 2025));
        dao.insert(holiday(LocalDate.of(2025, 7, 4), "Independence Day", "US", 2025));

        dao.deleteByCountryCode("IT");

        List<BankHoliday> result = dao.getByYear(2025);
        assertEquals(1, result.size());
        assertEquals("US", result.get(0).countryCode);
    }

    @Test
    public void getDatesForYear_returnsLocalDateList() {
        dao.insert(holiday(LocalDate.of(2025, 1, 1), "New Year", "IT", 2025));
        dao.insert(holiday(LocalDate.of(2025, 12, 25), "Christmas", "IT", 2025));

        List<LocalDate> dates = dao.getDatesForYear(2025);
        assertEquals(2, dates.size());
        assertTrue(dates.contains(LocalDate.of(2025, 1, 1)));
        assertTrue(dates.contains(LocalDate.of(2025, 12, 25)));
    }

    @Test
    public void customHoliday_hasNullCountryCode() {
        BankHoliday h = holiday(LocalDate.of(2025, 3, 15), "Team offsite", null, 2025);
        long id = dao.insert(h);
        h.id = id;

        BankHoliday result = dao.getByYear(2025).get(0);
        assertNull(result.countryCode);
        assertEquals("Team offsite", result.name);
    }

    private BankHoliday holiday(LocalDate date, String name, String countryCode, int year) {
        BankHoliday h = new BankHoliday();
        h.date = date;
        h.name = name;
        h.countryCode = countryCode;
        h.year = year;
        return h;
    }
}
