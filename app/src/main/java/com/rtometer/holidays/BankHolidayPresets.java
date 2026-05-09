package com.rtometer.holidays;

import com.rtometer.data.model.BankHolidayCountry;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BankHolidayPresets {

    public static List<LocalDate> getHolidays(BankHolidayCountry country, int year) {
        switch (country) {
            case IT: return italy(year);
            case GB: return unitedKingdom(year);
            case US: return unitedStates(year);
            case DE: return germany(year);
            case FR: return france(year);
            default: throw new IllegalArgumentException("Unknown country: " + country);
        }
    }

    private static List<LocalDate> italy(int year) {
        LocalDate easter = easterSunday(year);
        List<LocalDate> list = new ArrayList<>();
        list.add(LocalDate.of(year, 1, 1));   // New Year's Day
        list.add(LocalDate.of(year, 1, 6));   // Epiphany
        list.add(easter.plusDays(1));          // Easter Monday
        list.add(LocalDate.of(year, 4, 25));  // Liberation Day
        list.add(LocalDate.of(year, 5, 1));   // Labour Day
        list.add(LocalDate.of(year, 6, 2));   // Republic Day
        list.add(LocalDate.of(year, 8, 15));  // Assumption of Mary
        list.add(LocalDate.of(year, 11, 1));  // All Saints' Day
        list.add(LocalDate.of(year, 12, 8));  // Immaculate Conception
        list.add(LocalDate.of(year, 12, 25)); // Christmas Day
        list.add(LocalDate.of(year, 12, 26)); // St. Stephen's Day
        return list;
    }

    private static List<LocalDate> unitedKingdom(int year) {
        LocalDate easter = easterSunday(year);
        List<LocalDate> list = new ArrayList<>();
        list.add(LocalDate.of(year, 1, 1));         // New Year's Day
        list.add(easter.minusDays(2));               // Good Friday
        list.add(easter.plusDays(1));                // Easter Monday
        list.add(nthWeekday(year, 5, DayOfWeek.MONDAY, 1)); // Early May bank holiday
        list.add(lastWeekday(year, 5, DayOfWeek.MONDAY));   // Spring bank holiday
        list.add(lastWeekday(year, 8, DayOfWeek.MONDAY));   // Summer bank holiday
        list.add(LocalDate.of(year, 12, 25));        // Christmas Day
        list.add(LocalDate.of(year, 12, 26));        // Boxing Day
        return list;
    }

    private static List<LocalDate> unitedStates(int year) {
        List<LocalDate> list = new ArrayList<>();
        list.add(LocalDate.of(year, 1, 1));                              // New Year's Day
        list.add(nthWeekday(year, 1, DayOfWeek.MONDAY, 3));             // MLK Day
        list.add(nthWeekday(year, 2, DayOfWeek.MONDAY, 3));             // Presidents' Day
        list.add(lastWeekday(year, 5, DayOfWeek.MONDAY));               // Memorial Day
        list.add(LocalDate.of(year, 6, 19));                             // Juneteenth
        list.add(LocalDate.of(year, 7, 4));                              // Independence Day
        list.add(nthWeekday(year, 9, DayOfWeek.MONDAY, 1));             // Labor Day
        list.add(nthWeekday(year, 10, DayOfWeek.MONDAY, 2));            // Columbus Day
        list.add(LocalDate.of(year, 11, 11));                            // Veterans Day
        list.add(nthWeekday(year, 11, DayOfWeek.THURSDAY, 4));          // Thanksgiving
        list.add(LocalDate.of(year, 12, 25));                            // Christmas Day
        return list;
    }

    private static List<LocalDate> germany(int year) {
        LocalDate easter = easterSunday(year);
        List<LocalDate> list = new ArrayList<>();
        list.add(LocalDate.of(year, 1, 1));   // New Year's Day
        list.add(easter.minusDays(2));         // Good Friday
        list.add(easter.plusDays(1));          // Easter Monday
        list.add(LocalDate.of(year, 5, 1));   // Labour Day
        list.add(easter.plusDays(39));         // Ascension Day
        list.add(easter.plusDays(50));         // Whit Monday
        list.add(LocalDate.of(year, 10, 3));  // German Unity Day
        list.add(LocalDate.of(year, 12, 25)); // Christmas Day
        list.add(LocalDate.of(year, 12, 26)); // 2nd Day of Christmas
        return list;
    }

    private static List<LocalDate> france(int year) {
        LocalDate easter = easterSunday(year);
        List<LocalDate> list = new ArrayList<>();
        list.add(LocalDate.of(year, 1, 1));   // New Year's Day
        list.add(easter.plusDays(1));          // Easter Monday
        list.add(LocalDate.of(year, 5, 1));   // Labour Day
        list.add(LocalDate.of(year, 5, 8));   // Victory in Europe Day
        list.add(easter.plusDays(39));         // Ascension Day
        list.add(easter.plusDays(50));         // Whit Monday
        list.add(LocalDate.of(year, 7, 14));  // Bastille Day
        list.add(LocalDate.of(year, 8, 15));  // Assumption of Mary
        list.add(LocalDate.of(year, 11, 1));  // All Saints' Day
        list.add(LocalDate.of(year, 11, 11)); // Armistice Day
        list.add(LocalDate.of(year, 12, 25)); // Christmas Day
        return list;
    }

    // Anonymous Gregorian algorithm
    static LocalDate easterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }

    // Returns the nth occurrence of a weekday in a given month (1-indexed)
    private static LocalDate nthWeekday(int year, int month, DayOfWeek dow, int n) {
        LocalDate first = LocalDate.of(year, month, 1);
        int daysToTarget = (dow.getValue() - first.getDayOfWeek().getValue() + 7) % 7;
        return first.plusDays(daysToTarget + 7L * (n - 1));
    }

    // Returns the last occurrence of a weekday in a given month
    private static LocalDate lastWeekday(int year, int month, DayOfWeek dow) {
        LocalDate last = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
        int daysBack = (last.getDayOfWeek().getValue() - dow.getValue() + 7) % 7;
        return last.minusDays(daysBack);
    }
}
