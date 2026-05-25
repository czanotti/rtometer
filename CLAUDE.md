# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Purpose

**RTOmeter** is an Android app that tracks Return-To-Office attendance against a configurable quarterly target. It detects office presence automatically via GPS geofencing and lets users review, edit, and bulk-assign daily statuses from a calendar view.

## Agentic Workflow (read this first)

This repo follows Matt Pocock's agentic development principles. Before writing any code:

1. Pull [`ai/agentic_development.md`](ai/agentic_development.md) for the full operating protocol.
2. Run the `grill-me` skill ([`ai/skills/grill-me/SKILL.md`](ai/skills/grill-me/SKILL.md)) to reach shared design alignment.
3. Write or reference a PRD in [`ai/prds/`](ai/prds/) before creating GitHub Issues.
4. Break the PRD into vertical-slice GitHub Issues (each touches UI + API + DB).
5. Implement via TDD (Red → Green → Refactor). Never write implementation before a failing test.
6. One ticket per session. Stop after the PR is open; start a new session for the next ticket.

## `ai/` Folder

| Path | Purpose |
|------|---------|
| [`ai/agentic_development.md`](ai/agentic_development.md) | Matt Pocock principles — smart zone, tracer bullets, deep modules, TDD |
| [`ai/skills/`](ai/skills/) | Agent skills invoked by name (e.g. `grill-me`) |
| [`ai/prds/`](ai/prds/) | PRD per Epic/Feature; each links to its GitHub Issues |
| [`ai/progress.txt`](ai/progress.txt) | Flat list of completed and open tickets |

## Stack

| Layer | Technology |
|-------|-----------|
| Language | Java (Android SDK) |
| Architecture | MVVM — `ViewModel` + `LiveData` + `Repository` |
| DI | Hilt |
| Database | Room + SQLCipher encryption |
| Background work | WorkManager (periodic GPS checks) |
| Location | FusedLocationProviderClient (GPS geofencing) |
| UI | Fragments + ViewBinding, Material 3 (DayNight theme) |
| Testing | JUnit 4, Robolectric (for Android unit tests) |
| CI | GitHub Actions (`.github/workflows/ci.yml`) |

## Package Structure

```
com.rtometer
├── calculator/      QuarterCalculator, QuarterStats, MonthStats, PaceStatus
├── data/
│   ├── db/         Room entities (Quarter, AttendanceDay, Office, AppConfig), DAOs, AppDatabase
│   └── model/      DayStatus enum, domain models
├── gps/            GpsDetectionWorker, GpsDetectionModule
├── ui/
│   ├── dashboard/  DashboardFragment, BurndownView
│   ├── calendar/   CalendarFragment, CalendarAdapter
│   ├── history/    HistoryFragment, HistoryAdapter
│   ├── settings/   SettingsFragment, SettingsViewModel
│   └── main/       MainActivity, MainViewModel
└── onboarding/     OnboardingActivity and step fragments
```

## Dev Commands

```bash
# Run unit tests (no device/emulator required)
./gradlew :app:testDebugUnitTest

# Run a single test class
./gradlew :app:testDebugUnitTest --tests "com.rtometer.calculator.QuarterCalculatorTest"

# Build debug APK
./gradlew :app:assembleDebug
```

## CI/CD

Defined in [`.github/workflows/ci.yml`](.github/workflows/ci.yml):

- **On PR to main**: lint → unit tests → build
- **On merge to main**: lint → unit tests → build

## TDD Rules

- **Red first**: add a failing test before writing any production code.
- **Green**: write the minimum implementation to make it pass.
- **Refactor**: clean up without breaking tests.
- Never commit implementation-only changes without a corresponding test.

## Key Design Decisions

- `QuarterCalculator` is a pure-static class — no Android deps, fully unit-testable.
- SICK and HOLIDAY days are excluded from both the working-day denominator and `daysRemaining`.
- GPS skips weekends, outside working hours, and days already classified.
- `AppDatabase` uses SQLCipher; the passphrase is stored in the Android Keystore.
- `FLAG_SECURE` is set on all Activities to prevent screenshots and Recents thumbnails.
