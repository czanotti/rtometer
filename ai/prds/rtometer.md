# PRD: RTOmeter

**Epic:** Track Return to Office Attendance
**Package:** `com.rtometer`
**License:** MIT

---

## Problem Statement

Return-to-office mandates require employees to hit a minimum attendance threshold within a fiscal period — typically 50% of working days per quarter. Tracking this manually is error-prone, and no purpose-built Android app exists that handles fiscal (non-calendar) quarters, GPS-based check-in, and the nuances of holidays and sick days correctly. Without real-time visibility, employees often realize they need to adjust their attendance patterns with little time left in the quarter.

## Solution

RTOmeter is a local-first Android app (Java, no backend, no account required) that automatically detects office presence via GPS geofencing and gives the user a real-time view of their attendance percentage against their quarterly target. The app is open source (MIT) and designed to be useful globally — fiscal quarter layouts, attendance targets, office locations, and bank holiday presets are all configurable.

## User Stories

1. As a user, I want to configure my fiscal quarter layout from a preset list, so that the app matches my company's fiscal year without manual date entry.
2. As a user, I want to set my attendance target percentage, so that the app tracks against my company's specific mandate.
3. As a user, I want to register one or more office locations with a GPS coordinate and radius, so that the app can detect when I am physically at the office.
4. As a user, I want the app to automatically detect my office presence during work hours and stop polling GPS once confirmed, so that my attendance is logged without me opening the app and my battery is not drained.
5. As a user, I want to see my current attendance percentage, pace indicator, and days remaining at a glance, so that I know whether I am on track without doing any mental math.
6. As a user, I want to view a full calendar of the current quarter showing which days I was in, out, sick, or on holiday, so that I can audit my attendance history.
7. As a user, I want to manually override any day's status from the calendar, so that I can correct GPS failures or missed detections.
8. As a user, I want bank holidays pre-loaded for my country and editable, so that public holidays are automatically excluded from my working day count.
9. As a user, I want to mark individual days as sick, so that illness does not penalise my attendance percentage.
10. As a user, I want to mark individual days as personal holiday, so that approved leave does not penalise my attendance percentage.
11. As a user installing mid-quarter, I want to either select the specific days I already went to the office or enter a total count, so that my historical attendance is not lost.
12. As a user, I want to view past quarters' attendance records, so that I can verify historical compliance.
13. As a user who denies location permission, I want the app to work as a manual tracker with a clear warning, so that I can still use it without background location.
14. As an open source contributor, I want to add bank holiday presets for new countries, so that users in my country can benefit from the app.

## Implementation Decisions

### Architecture
- **Language:** Java
- **UI:** XML layouts (no Compose)
- **Pattern:** MVVM — ViewModel + LiveData + Repository
- **Database:** Room (local only, no cloud sync)
- **Background work:** WorkManager for GPS polling scheduler
- **Dependency injection:** Hilt
- **Min SDK:** API 26 (Android 8.0)
- **Target SDK:** API 35

### Data Model

#### `Quarter`
| Field | Type | Notes |
|---|---|---|
| id | long (PK) | |
| fiscalYear | int | Calendar year + 1 per company convention |
| quarterNumber | int | 1–4 |
| startDate | LocalDate | |
| endDate | LocalDate | |
| targetPercentage | float | Default 0.5 |

#### `Office`
| Field | Type | Notes |
|---|---|---|
| id | long (PK) | |
| name | String | |
| latitude | double | |
| longitude | double | |
| radiusMeters | int | Default 200 |
| isPrimary | boolean | |

#### `AttendanceDay`
| Field | Type | Notes |
|---|---|---|
| id | long (PK) | |
| date | LocalDate | |
| quarterId | long (FK) | |
| status | Enum | `IN_OFFICE`, `NOT_IN_OFFICE`, `SICK`, `HOLIDAY` |
| isManualOverride | boolean | Shown in UI when true |
| detectedOfficeId | long (FK, nullable) | Which office triggered detection |

#### `AppConfig`
| Field | Type | Notes |
|---|---|---|
| workDayStart | LocalTime | Default 08:00 |
| workDayEnd | LocalTime | Default 18:00 |
| gpsIntervalMinutes | int | Default 15 |
| bankHolidayCountry | String | ISO country code |

### Modules

- **OnboardingModule** — mandatory first-launch wizard: quarter preset → target % → work hours → GPS interval → office setup → bank holiday country → mid-quarter pre-load (pick days or enter count)
- **DashboardModule** — main tab: current %, target %, days in office, days not in office, days remaining in quarter, days needed to hit target, pace indicator (green/amber/red)
- **CalendarModule** — second tab: full month-by-month view of the current quarter; tap any day to see/edit status; manual overrides shown with a distinct marker
- **OfficeSetupModule** — add/edit/delete offices; set name, coordinate (map pin or current location), radius
- **HolidayModule** — manage bank holidays (country preset loader + manual add/remove) and personal holidays
- **HistoryModule** — past quarters list with summary stats per quarter
- **SettingsModule** — edit work hours, GPS interval, attendance target, quarter config, offices, holidays
- **GpsDetectionModule** — WorkManager periodic task; checks device location against all registered offices during work hours window; marks day `IN_OFFICE` and cancels further work for the day once detected; respects location permission state
- **QuarterCalculatorModule** — pure logic: computes working days in a quarter (Mon–Fri minus all holiday/sick days), days attended, percentage, days needed, pace status

### Fiscal Quarter Presets

Ship with the following presets (community-contributed additional presets go in a separate `holidays/` package):
- **Calendar quarters** — Jan, Apr, Jul, Oct starts
- **Feb-start** (Cristiano's default) — Feb, May, Aug, Nov starts
- **Apr-start** — Apr, Jul, Oct, Jan starts
- **May-start** — May, Aug, Nov, Feb starts
- **Custom** — user defines Q1 start month; quarters are always 3 months each

Fiscal year label = calendar year of Q1 start + 1 (configurable offset, default +1).

### Bank Holiday Presets (v1)

Ship with: 🇮🇹 Italy, 🇬🇧 United Kingdom, 🇺🇸 United States, 🇩🇪 Germany, 🇫🇷 France.
Presets are hardcoded per calendar year — community can contribute via PRs adding new countries and updating years.

### GPS Smart Polling Logic

```
Each working day, during [workDayStart, workDayEnd]:
  Schedule WorkManager periodic task every gpsIntervalMinutes
  On each run:
    If day already marked IN_OFFICE → cancel remaining work for today
    If location permission denied → skip silently (app shows warning banner)
    Check current location against all office geofences
    If within radius of any office → mark day IN_OFFICE, cancel remaining work
  At workDayEnd:
    If day still NOT_IN_OFFICE → mark as NOT_IN_OFFICE (auto)
```

GPS polling does not run on weekends, bank holidays, personal holidays, or sick days.

### Pace Indicator Logic

```
daysRemaining = working days left in quarter (excl. holidays)
daysNeeded = ceil(target% × totalWorkingDays) - daysAttended
paceStatus:
  GREEN  → daysNeeded ≤ daysRemaining × target%
  AMBER  → daysNeeded ≤ daysRemaining (achievable but tight)
  RED    → daysNeeded > daysRemaining (mathematically impossible to hit target)
```

### Mid-Quarter Pre-load (Onboarding)

Two paths offered, user picks one:
- **Pick days** — calendar picker showing all past Mon–Fri of the current quarter; user taps attended days; each is stored as `IN_OFFICE` with `isManualOverride = true`
- **Enter count** — numeric input; stored as a synthetic `AttendanceDay` aggregate with no specific dates; UI displays a banner warning that exact dates were not recorded for this count

### Manual Override UI

On the calendar, any day the user taps shows a bottom sheet with status options: In Office / Not in Office / Sick / Holiday. Days with `isManualOverride = true` show a small pencil icon. No reason field required.

### Location Permission Handling

On first launch (or when GPS module first runs):
- If background location denied → show a persistent non-blocking banner: "GPS detection is off. Days will not be auto-detected — log them manually." Banner links to system settings.
- App is fully functional as a manual tracker without location permission.

## Testing Decisions

Good tests assert external behavior observable by a user or caller — not internal implementation details.

### Units to test
- **QuarterCalculatorModule** — given a quarter with known Mon–Fri count and a set of holidays/sick days, assert correct `totalWorkingDays`, `daysAttended`, `percentage`, `daysNeeded`, `paceStatus`
- **FiscalQuarterPreset** — given each preset + a start year, assert correct start/end dates for all 4 quarters
- **GpsDetectionModule** — given device coordinates within office radius, assert day status transitions to `IN_OFFICE`; given coordinates outside radius, assert no state change
- **AttendanceRepository** — given Room in-memory database, assert CRUD operations and quarter rollover archival

### Test setup
- JUnit 4 + Mockito for unit tests
- Robolectric for Android-dependent tests (Room, WorkManager)
- `InstantTaskExecutorRule` for LiveData testing

## Out of Scope (v1)

- Push notifications (weekly summaries, pace alerts, end-of-day prompts)
- Cloud backup or sync
- Data export (CSV, PDF)
- Home screen widget
- Dark/light mode toggle (follow system default)
- Multiple user profiles
- Configurable work week (Mon–Fri is fixed)
- 4-4-5 or unequal quarter calendars
- Play Store publication (sideload / GitHub releases only for v1)

## Further Notes

- The fiscal year label convention (calendar year + 1) is Cristiano's default but should be a configurable offset (0 or +1) in the quarter setup screen — some companies label fiscal years by the calendar year they start in.
- The "count only" pre-load path must store enough metadata to distinguish it from day-by-day records in the percentage calculation — a `preloadCount` field on the `Quarter` entity is cleaner than creating synthetic `AttendanceDay` rows with no date.
- WorkManager constraints for the GPS task: `NetworkType.NOT_REQUIRED`, `requiresBatteryNotLow = false` (attendance detection is critical). Use `PeriodicWorkRequest` with `REPLACE` existing work policy so interval changes in settings take effect immediately.
- Office detection should check all registered offices on each poll, not just the primary one — a day attended at any office counts.
- The open source repo should include a `CONTRIBUTING.md` explaining how to add a new bank holiday country preset and how to add a new fiscal quarter preset.

## Issues

| # | Title | Type | Blocked by |
|---|---|---|---|
| RTO-1 | PRD: RTOmeter | — | — |
| RTO-2 | Project scaffold: Hilt + Room + WorkManager + MVVM base + GitHub Actions CI | HITL | RTO-1 |
| RTO-3 | Data model: Quarter, Office, AttendanceDay, AppConfig (Room entities + DAOs) | AFK | RTO-2 |
| RTO-4 | QuarterCalculatorModule: working days, percentage, pace logic | AFK | RTO-3 |
| RTO-5 | Fiscal quarter presets + custom config | AFK | RTO-3 |
| RTO-6 | Bank holiday presets (IT, GB, US, DE, FR) + manual add/remove | AFK | RTO-3 |
| RTO-7 | Onboarding wizard (6 screens: quarter → target → hours → office → holidays → pre-load) | AFK | RTO-5, RTO-6 |
| RTO-8 | GpsDetectionModule: WorkManager periodic task + geofence check | AFK | RTO-3 |
| RTO-9 | DashboardModule: percentage, pace indicator, summary stats | AFK | RTO-4 |
| RTO-10 | CalendarModule: quarter calendar view + day status tap/edit | AFK | RTO-3 |
| RTO-11 | OfficeSetupModule: map pin + current location + radius config | AFK | RTO-3 |
| RTO-12 | HistoryModule: past quarters archive + summary stats | AFK | RTO-4 |
| RTO-13 | SettingsModule: edit all app config | AFK | RTO-7 |
| RTO-14 | Location permission handling + degraded-mode banner | AFK | RTO-8 |
| RTO-15 | Unit tests: Calculator, Presets, GPS detection, Repository | AFK | RTO-4, RTO-8 |
| RTO-16 | README + CONTRIBUTING.md (holiday presets, quarter presets) | AFK | RTO-15 |
