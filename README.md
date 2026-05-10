# RTOmeter

A local-first Android app for tracking Return-to-Office attendance against your company's quarterly RTO policy.

## What it does

RTOmeter counts the days you go to the office each quarter and shows your compliance percentage against a configurable target. It can detect office presence automatically via GPS geofencing, or work entirely as a manual tracker if location access is not granted.

**Key features:**

- Dashboard with real-time attendance percentage and a Green/Amber/Red pace indicator
- GPS-based office detection — set your office location and a geofence radius; the app polls in the background via WorkManager
- Manual override — tap any day to set its status (In Office, Not in Office, Sick, Holiday)
- Quarter calendar view — month-by-month status for the current quarter
- Fiscal quarter flexibility — calendar year, or preset/custom fiscal year start months
- Bank holiday presets for Italy, United Kingdom, United States, Germany, and France
- Multi-quarter history archive with summary statistics
- Mid-quarter pre-load for entering historical attendance when you first install
- Fully local — no account, no backend, no cloud sync

## Requirements

- Android 8.0 (API 26) or higher
- Android Studio Hedgehog or later (AGP 8.7.3, Gradle 8.9)
- JDK 17

## Build and sideload

```bash
# Clone the repo
git clone https://github.com/czanotti/rtometer.git
cd rtometer

# Build a debug APK
./gradlew assembleDebug

# Install directly to a connected device or emulator
./gradlew installDebug
```

The resulting APK is at `app/build/outputs/apk/debug/app-debug.apk`. Transfer it to your device and open it to sideload.

## Supported presets

### Bank holiday countries

| Code | Country        |
|------|----------------|
| IT   | Italy          |
| GB   | United Kingdom |
| US   | United States  |
| DE   | Germany        |
| FR   | France         |

### Fiscal quarter presets

| Preset        | Q1 starts | Typical use              |
|---------------|-----------|--------------------------|
| CALENDAR      | January   | Standard calendar year   |
| FEB_START     | February  |                          |
| APR_START     | April     | UK fiscal year           |
| MAY_START     | May       |                          |
| CUSTOM        | Any month | User-defined start month |

## License

MIT — see [LICENSE](LICENSE).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to add new bank holiday country presets and fiscal quarter presets.
