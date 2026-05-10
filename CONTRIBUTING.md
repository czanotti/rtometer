# Contributing to RTOmeter

## Adding a bank holiday country preset

Bank holiday country presets live in two places:

1. **`BankHolidayCountry` enum** — declares the country code.
2. **`BankHolidayPresets` class** — provides the actual holiday list for each country and year.

### Step 1 — add the country code

Open `app/src/main/java/com/rtometer/data/model/BankHolidayCountry.java` and add your ISO 3166-1 alpha-2 country code to the enum:

```java
public enum BankHolidayCountry {
    IT, GB, US, DE, FR,
    CA   // ← your new entry
}
```

### Step 2 — implement the holiday list

Open `app/src/main/java/com/rtometer/holidays/BankHolidayPresets.java`.

Add a `case` for your country inside the method that dispatches by `BankHolidayCountry`. Return a `List<LocalDate>` containing every public holiday for the requested year. Use the existing country implementations as a reference — they cover fixed-date holidays (e.g. Christmas on Dec 25) and computed holidays (e.g. Easter Monday).

```java
case CA:
    return canadaHolidays(year);
```

Then implement the helper:

```java
private static List<LocalDate> canadaHolidays(int year) {
    List<LocalDate> days = new ArrayList<>();
    days.add(LocalDate.of(year, 1, 1));   // New Year's Day
    days.add(LocalDate.of(year, 7, 1));   // Canada Day
    // … add all statutory holidays …
    return days;
}
```

### Step 3 — write a unit test

Add a test class in `app/src/test/java/com/rtometer/holidays/` that verifies the count and spot-checks specific dates for at least one year. Follow the pattern of existing holiday tests.

### Step 4 — verify

```bash
./gradlew testDebugUnitTest
```

All existing tests must remain green.

---

## Adding a fiscal quarter preset

Fiscal quarter presets are defined by their start month. The full implementation is in two files:

1. **`FiscalQuarterPreset` enum** — declares named presets.
2. **`FiscalQuarterFactory`** — creates `Quarter` objects from a preset.

### Step 1 — add the preset

Open `app/src/main/java/com/rtometer/calculator/FiscalQuarterPreset.java` and add your new entry with the month number (1 = January, 12 = December) when the fiscal year starts:

```java
public enum FiscalQuarterPreset {
    CALENDAR(1),
    FEB_START(2),
    APR_START(4),
    MAY_START(5),
    AUG_START(8),  // ← your new entry
    CUSTOM(-1);

    public final int startMonth;

    FiscalQuarterPreset(int startMonth) {
        this.startMonth = startMonth;
    }
}
```

The factory uses `startMonth` directly to compute quarter boundaries — no further changes to `FiscalQuarterFactory` are needed for a standard 3-month quarter.

### Step 2 — write a unit test

Add a test in `app/src/test/java/com/rtometer/calculator/` that verifies the quarter boundaries produced by your new preset. Check at least the start date of Q1 and that Q4 ends correctly.

### Step 3 — verify

```bash
./gradlew testDebugUnitTest
```

All existing tests must remain green.
