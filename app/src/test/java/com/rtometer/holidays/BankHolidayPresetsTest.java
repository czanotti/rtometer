package com.rtometer.holidays;

import com.rtometer.data.model.BankHolidayCountry;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class BankHolidayPresetsTest {

    // --- Italy ---

    @Test
    public void italy_2025_containsNewYear() {
        assertTrue(holidays(BankHolidayCountry.IT, 2025).contains(LocalDate.of(2025, 1, 1)));
    }

    @Test
    public void italy_2025_containsEpiphany() {
        assertTrue(holidays(BankHolidayCountry.IT, 2025).contains(LocalDate.of(2025, 1, 6)));
    }

    @Test
    public void italy_2025_containsEasterMonday() {
        // Easter 2025 = Apr 20, Easter Monday = Apr 21
        assertTrue(holidays(BankHolidayCountry.IT, 2025).contains(LocalDate.of(2025, 4, 21)));
    }

    @Test
    public void italy_2025_containsLiberationDay() {
        assertTrue(holidays(BankHolidayCountry.IT, 2025).contains(LocalDate.of(2025, 4, 25)));
    }

    @Test
    public void italy_2025_containsLabourDay() {
        assertTrue(holidays(BankHolidayCountry.IT, 2025).contains(LocalDate.of(2025, 5, 1)));
    }

    @Test
    public void italy_2025_containsRepublicDay() {
        assertTrue(holidays(BankHolidayCountry.IT, 2025).contains(LocalDate.of(2025, 6, 2)));
    }

    @Test
    public void italy_2025_containsAssumption() {
        assertTrue(holidays(BankHolidayCountry.IT, 2025).contains(LocalDate.of(2025, 8, 15)));
    }

    @Test
    public void italy_2025_containsAllSaints() {
        assertTrue(holidays(BankHolidayCountry.IT, 2025).contains(LocalDate.of(2025, 11, 1)));
    }

    @Test
    public void italy_2025_containsImmaculateConception() {
        assertTrue(holidays(BankHolidayCountry.IT, 2025).contains(LocalDate.of(2025, 12, 8)));
    }

    @Test
    public void italy_2025_containsChristmas() {
        assertTrue(holidays(BankHolidayCountry.IT, 2025).contains(LocalDate.of(2025, 12, 25)));
    }

    @Test
    public void italy_2025_containsStStephen() {
        assertTrue(holidays(BankHolidayCountry.IT, 2025).contains(LocalDate.of(2025, 12, 26)));
    }

    @Test
    public void italy_2025_hasElevenHolidays() {
        assertEquals(11, holidays(BankHolidayCountry.IT, 2025).size());
    }

    // --- United Kingdom ---

    @Test
    public void uk_2025_containsNewYear() {
        assertTrue(holidays(BankHolidayCountry.GB, 2025).contains(LocalDate.of(2025, 1, 1)));
    }

    @Test
    public void uk_2025_containsGoodFriday() {
        // Easter 2025 = Apr 20, Good Friday = Apr 18
        assertTrue(holidays(BankHolidayCountry.GB, 2025).contains(LocalDate.of(2025, 4, 18)));
    }

    @Test
    public void uk_2025_containsEasterMonday() {
        assertTrue(holidays(BankHolidayCountry.GB, 2025).contains(LocalDate.of(2025, 4, 21)));
    }

    @Test
    public void uk_2025_containsEarlyMayBankHoliday() {
        // 1st Monday in May 2025 = May 5
        assertTrue(holidays(BankHolidayCountry.GB, 2025).contains(LocalDate.of(2025, 5, 5)));
    }

    @Test
    public void uk_2025_containsSpringBankHoliday() {
        // last Monday in May 2025 = May 26
        assertTrue(holidays(BankHolidayCountry.GB, 2025).contains(LocalDate.of(2025, 5, 26)));
    }

    @Test
    public void uk_2025_containsSummerBankHoliday() {
        // last Monday in August 2025 = Aug 25
        assertTrue(holidays(BankHolidayCountry.GB, 2025).contains(LocalDate.of(2025, 8, 25)));
    }

    @Test
    public void uk_2025_containsChristmas() {
        assertTrue(holidays(BankHolidayCountry.GB, 2025).contains(LocalDate.of(2025, 12, 25)));
    }

    @Test
    public void uk_2025_containsBoxingDay() {
        assertTrue(holidays(BankHolidayCountry.GB, 2025).contains(LocalDate.of(2025, 12, 26)));
    }

    @Test
    public void uk_2025_hasEightHolidays() {
        assertEquals(8, holidays(BankHolidayCountry.GB, 2025).size());
    }

    // --- United States ---

    @Test
    public void us_2025_containsNewYear() {
        assertTrue(holidays(BankHolidayCountry.US, 2025).contains(LocalDate.of(2025, 1, 1)));
    }

    @Test
    public void us_2025_containsMlkDay() {
        // 3rd Monday January 2025 = Jan 20
        assertTrue(holidays(BankHolidayCountry.US, 2025).contains(LocalDate.of(2025, 1, 20)));
    }

    @Test
    public void us_2025_containsPresidentsDay() {
        // 3rd Monday February 2025 = Feb 17
        assertTrue(holidays(BankHolidayCountry.US, 2025).contains(LocalDate.of(2025, 2, 17)));
    }

    @Test
    public void us_2025_containsMemorialDay() {
        // last Monday May 2025 = May 26
        assertTrue(holidays(BankHolidayCountry.US, 2025).contains(LocalDate.of(2025, 5, 26)));
    }

    @Test
    public void us_2025_containsJuneteenth() {
        assertTrue(holidays(BankHolidayCountry.US, 2025).contains(LocalDate.of(2025, 6, 19)));
    }

    @Test
    public void us_2025_containsIndependenceDay() {
        assertTrue(holidays(BankHolidayCountry.US, 2025).contains(LocalDate.of(2025, 7, 4)));
    }

    @Test
    public void us_2025_containsLaborDay() {
        // 1st Monday September 2025 = Sep 1
        assertTrue(holidays(BankHolidayCountry.US, 2025).contains(LocalDate.of(2025, 9, 1)));
    }

    @Test
    public void us_2025_containsColumbusDay() {
        // 2nd Monday October 2025 = Oct 13
        assertTrue(holidays(BankHolidayCountry.US, 2025).contains(LocalDate.of(2025, 10, 13)));
    }

    @Test
    public void us_2025_containsVeteransDay() {
        assertTrue(holidays(BankHolidayCountry.US, 2025).contains(LocalDate.of(2025, 11, 11)));
    }

    @Test
    public void us_2025_containsThanksgiving() {
        // 4th Thursday November 2025 = Nov 27
        assertTrue(holidays(BankHolidayCountry.US, 2025).contains(LocalDate.of(2025, 11, 27)));
    }

    @Test
    public void us_2025_containsChristmas() {
        assertTrue(holidays(BankHolidayCountry.US, 2025).contains(LocalDate.of(2025, 12, 25)));
    }

    @Test
    public void us_2025_hasElevenHolidays() {
        assertEquals(11, holidays(BankHolidayCountry.US, 2025).size());
    }

    // --- Germany ---

    @Test
    public void germany_2025_containsNewYear() {
        assertTrue(holidays(BankHolidayCountry.DE, 2025).contains(LocalDate.of(2025, 1, 1)));
    }

    @Test
    public void germany_2025_containsGoodFriday() {
        assertTrue(holidays(BankHolidayCountry.DE, 2025).contains(LocalDate.of(2025, 4, 18)));
    }

    @Test
    public void germany_2025_containsEasterMonday() {
        assertTrue(holidays(BankHolidayCountry.DE, 2025).contains(LocalDate.of(2025, 4, 21)));
    }

    @Test
    public void germany_2025_containsLabourDay() {
        assertTrue(holidays(BankHolidayCountry.DE, 2025).contains(LocalDate.of(2025, 5, 1)));
    }

    @Test
    public void germany_2025_containsAscensionDay() {
        // Easter Sunday Apr 20 + 39 days = May 29
        assertTrue(holidays(BankHolidayCountry.DE, 2025).contains(LocalDate.of(2025, 5, 29)));
    }

    @Test
    public void germany_2025_containsWhitMonday() {
        // Easter Sunday Apr 20 + 50 days (Pentecost + 1) = Jun 9
        assertTrue(holidays(BankHolidayCountry.DE, 2025).contains(LocalDate.of(2025, 6, 9)));
    }

    @Test
    public void germany_2025_containsGermanUnityDay() {
        assertTrue(holidays(BankHolidayCountry.DE, 2025).contains(LocalDate.of(2025, 10, 3)));
    }

    @Test
    public void germany_2025_containsChristmas() {
        assertTrue(holidays(BankHolidayCountry.DE, 2025).contains(LocalDate.of(2025, 12, 25)));
    }

    @Test
    public void germany_2025_containsSecondChristmasDay() {
        assertTrue(holidays(BankHolidayCountry.DE, 2025).contains(LocalDate.of(2025, 12, 26)));
    }

    @Test
    public void germany_2025_hasNineHolidays() {
        assertEquals(9, holidays(BankHolidayCountry.DE, 2025).size());
    }

    // --- France ---

    @Test
    public void france_2025_containsNewYear() {
        assertTrue(holidays(BankHolidayCountry.FR, 2025).contains(LocalDate.of(2025, 1, 1)));
    }

    @Test
    public void france_2025_containsEasterMonday() {
        assertTrue(holidays(BankHolidayCountry.FR, 2025).contains(LocalDate.of(2025, 4, 21)));
    }

    @Test
    public void france_2025_containsLabourDay() {
        assertTrue(holidays(BankHolidayCountry.FR, 2025).contains(LocalDate.of(2025, 5, 1)));
    }

    @Test
    public void france_2025_containsVEDay() {
        assertTrue(holidays(BankHolidayCountry.FR, 2025).contains(LocalDate.of(2025, 5, 8)));
    }

    @Test
    public void france_2025_containsAscensionDay() {
        assertTrue(holidays(BankHolidayCountry.FR, 2025).contains(LocalDate.of(2025, 5, 29)));
    }

    @Test
    public void france_2025_containsWhitMonday() {
        assertTrue(holidays(BankHolidayCountry.FR, 2025).contains(LocalDate.of(2025, 6, 9)));
    }

    @Test
    public void france_2025_containsBastilleDay() {
        assertTrue(holidays(BankHolidayCountry.FR, 2025).contains(LocalDate.of(2025, 7, 14)));
    }

    @Test
    public void france_2025_containsAssumption() {
        assertTrue(holidays(BankHolidayCountry.FR, 2025).contains(LocalDate.of(2025, 8, 15)));
    }

    @Test
    public void france_2025_containsAllSaints() {
        assertTrue(holidays(BankHolidayCountry.FR, 2025).contains(LocalDate.of(2025, 11, 1)));
    }

    @Test
    public void france_2025_containsArmisticeDay() {
        assertTrue(holidays(BankHolidayCountry.FR, 2025).contains(LocalDate.of(2025, 11, 11)));
    }

    @Test
    public void france_2025_containsChristmas() {
        assertTrue(holidays(BankHolidayCountry.FR, 2025).contains(LocalDate.of(2025, 12, 25)));
    }

    @Test
    public void france_2025_hasElevenHolidays() {
        assertEquals(11, holidays(BankHolidayCountry.FR, 2025).size());
    }

    // --- Easter algorithm (cross-year sanity check) ---

    @Test
    public void italy_2024_easterMondayIsApril1() {
        // Easter Sunday 2024 = March 31, Easter Monday = April 1
        assertTrue(holidays(BankHolidayCountry.IT, 2024).contains(LocalDate.of(2024, 4, 1)));
    }

    @Test
    public void italy_2026_easterMondayIsApril6() {
        // Easter Sunday 2026 = April 5, Easter Monday = April 6
        assertTrue(holidays(BankHolidayCountry.IT, 2026).contains(LocalDate.of(2026, 4, 6)));
    }

    // --- Return type is mutable (for DAO to build BankHoliday entities) ---

    @Test
    public void getHolidays_returnsNonNullList() {
        assertNotNull(BankHolidayPresets.getHolidays(BankHolidayCountry.IT, 2025));
    }

    // helper
    private List<LocalDate> holidays(BankHolidayCountry country, int year) {
        return BankHolidayPresets.getHolidays(country, year);
    }
}
