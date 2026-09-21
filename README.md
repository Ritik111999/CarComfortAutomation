# CarComfortAutomation

AI-operated full functional automation for Car Comfort — Android (Flutter, physical devices only) + Customer/Admin PWAs.

> Rules: see [AGENTS.md](AGENTS.md). Physical devices only, device locking mandatory, no `Thread.sleep()`, safe assertions, gated-test protection.

## Architecture

```
Tests (Business Intent)
    ↓
Business Flows (login, booking, payment)
    ↓
Component Objects (header, card, list item)
    ↓
Screen/Page Objects (LoginScreen, BookingScreen)
    ↓
Core (Driver/Device/Config/Wait/Assertion/Logging/Reporting/Evidence)
```

- Android locators priority: `accessibility-id` → `resource-id` → semantic text → UiAutomator → constrained XPath. Never coordinates as primary.
- Reports: `ReportEngine` (Extent) + `PdfReportGenerator`. Never call Extent directly from tests.
- Failures auto-capture screenshot + page source via `EvidenceCollector`.
- Failures classified: `APPLICATION_DEFECT`, `LOCATOR_DRIFT`, `SYNC_ISSUE`, `DEVICE_INFRASTRUCTURE`, `TEST_DATA`, `AUTOMATION_DEFECT`. Only auto-repair `LOCATOR_DRIFT`, `SYNC_ISSUE`, `AUTOMATION_DEFECT`.

## Prerequisites

- Java 21, Maven 3.9+
- Appium 2.x + UiAutomator2 driver
- Android SDK platform-tools (`adb`) + one **physical** Android device (USB, developer mode)
- Chrome for PWA tests
- macOS note: if `/usr/bin/git` fails with Xcode license error, use `/Library/Developer/CommandLineTools/usr/bin/git`

## Quickstart

```bash
# 1. Clone
git clone https://github.com/Ritik111999/CarComfortAutomation.git
cd CarComfortAutomation

# 2. Env
cp .env.example .env
# fill ANDROID_DEVICE_UDID, ANDROID_APP_PACKAGE, CUSTOMER_PWA_URL, etc.
# NEVER commit .env

# 3. Verify device
adb devices

# 4. Smoke (physical device must be locked via DeviceLockManager — handled in code)
mvn clean test -DsuiteXmlFile=src/test/resources/suites/smoke.xml
```

## Suites

| Suite | File | Purpose |
|-------|------|---------|
| Smoke | `src/test/resources/suites/smoke.xml` | Safe first scenario, device + Appium + reporting verify |
| Android regression | `src/test/resources/suites/android-regression.xml` | Customer/Provider/Admin Android flows |
| PWA regression | `src/test/resources/suites/pwa-regression.xml` | Customer/Admin PWA flows |
| Gated one-time | `src/test/resources/suites/gated-one-time.xml` | Irreversible / OTP / payment / bootstrap — blocked by default |

Gated tests require explicit authorization:

```bash
mvn test -DsuiteXmlFile=src/test/resources/suites/gated-one-time.xml \
  -DRUN_GATED_TESTS=true -DGATED_CASE=CARD_SETUP
```

Categories: `GATED`, `ONE_TIME`, `VERIFICATION`, `PAYMENT_SETUP`, `ACCOUNT_BOOTSTRAP`, `DESTRUCTIVE`.

## Repo structure

```
car-comfort-automation/
├── AGENTS.md
├── pom.xml
├── .env.example
├── src/test/java/com/carcomfort/
│   ├── core/               # config, driver, device, waits, assertions, logging, reporting, evidence, guards
│   ├── mobile/android/     # screens + components
│   ├── web/customer/       # Customer PWA pages
│   ├── web/admin/          # Admin PWA pages
│   ├── flows/              # Business flows
│   └── tests/              # Test classes (business intent only)
├── src/test/resources/
│   ├── config/             # HOCON configs
│   ├── testdata/           # YAML test data
│   └── suites/             # TestNG suites
├── docs/                   # architecture, coverage, flows, exploration, decisions, blockers
├── scripts/                # utility scripts
└── artifacts/              # generated: logs, reports, evidence, locks (git-ignored)
```

## Docs (update after every automation change)

- `docs/exploration/SCREEN_CATALOG.md`
- `docs/flows/FLOW_CATALOG.md`
- `docs/coverage/COVERAGE_MATRIX.md`
- `docs/automation/KNOWN_BLOCKERS.md`
- `docs/decisions/ARCHITECTURE_DECISIONS.md`

## CI

GitHub Actions (`.github/workflows/maven.yml`) runs `mvn -B clean compile` on push/PR. Device tests run locally on physical hardware only — never on CI emulators.

## Environment variables

See [.env.example](.env.example):

```
ANDROID_DEVICE_UDID=
ANDROID_DEVICE_NAME=
ANDROID_APP_PACKAGE=
ANDROID_APP_ACTIVITY=
CUSTOMER_PWA_URL=
ADMIN_PWA_URL=
AUTOMATION_OWNER_ID=
RUN_GATED_TESTS=false
GATED_CASE=
```
