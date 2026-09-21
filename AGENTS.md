# Car Comfort Automation - AGENTS.md

## Permanent Global Rules for All AI Agents

### CORE PRINCIPLES

1. **Physical Devices Only** - NEVER use emulators/simulators. All Android/iOS automation runs on real physical devices.
2. **Device Locking Mandatory** - Before using any physical device, acquire lock via `DeviceLockManager`. Release in `finally` block.
3. **No Thread.sleep()** - Use explicit waits only (`WaitStrategies`, `AndroidWaitStrategies`).
4. **Safe Assertions** - Use `SafeAssertions` with soft assertions. Never weaken expected values to match actual.
5. **Gated Test Protection** - Tests tagged with `GATED`, `ONE_TIME`, `VERIFICATION`, `PAYMENT_SETUP`, `ACCOUNT_BOOTSTRAP`, `DESTRUCTIVE` require `RUN_GATED_TESTS=true` AND `GATED_CASE=<case>`.
6. **Secret Handling** - Never log passwords, OTPs, tokens, CVV, PII. Use `TestLogger.mask()` and `TestDataManager.maskForLogging()`.
7. **Evidence on Failure** - Auto-capture screenshot + page source on assertion failure via `EvidenceCollector`.
8. **Report Abstraction** - Use `ReportEngine` (Extent) + `PdfReportGenerator`. Never couple tests to Extent directly.
9. **Coverage Tracking** - Update `docs/coverage/COVERAGE_MATRIX.md` after every automation addition.

### ARCHITECTURE LAYERS (Must Follow)

```
Tests (Business Intent)
    ↓
Business Flows (Reusable workflows: login, booking, payment)
    ↓
Component Objects (Reusable UI fragments: header, card, list item)
    ↓
Screen/Page Objects (Full screens: LoginScreen, BookingScreen)
    ↓
Driver/Device/Config/Wait/Assertion/Logging/Reporting/Evidence (Core)
```

### LOCATOR PRIORITY (Android Flutter App)

1. `accessibility-id` (content-desc)
2. `resource-id`
3. Stable semantic text
4. UiAutomator selectors
5. Constrained XPath
6. **NEVER** coordinates as primary strategy

### GATED TEST CATEGORIES (Auto-blocked)

| Category | Examples |
|----------|----------|
| `GATED` | Any irreversible action |
| `ONE_TIME` | Account creation, initial setup |
| `VERIFICATION` | OTP, KYC, Stripe verification |
| `PAYMENT_SETUP` | Card/bank addition, Stripe onboarding |
| `ACCOUNT_BOOTSTRAP` | First-time role onboarding |
| `DESTRUCTIVE` | Delete account, cancel irreversible |

**To run**: `mvn test -DsuiteXmlFile=src/test/resources/suites/gated-one-time.xml -D RUN_GATED_TESTS=true -D GATED_CASE=CARD_SETUP`

### DEVICE MANAGEMENT

- Each device has: platform, deviceName, udid, osVersion, availability, currentOwner, lockStatus, lastHealthCheck
- Lock file: `artifacts/device-locks/<platform>:<udid>.lock`
- Heartbeat every 10s, stale cleanup after 600s
- Only ONE agent/process per device at a time

### FAILURE CLASSIFICATION

When test fails, classify as:
- `APPLICATION_DEFECT` - Product bug
- `LOCATOR_DRIFT` - UI changed, fix locator
- `SYNC_ISSUE` - Timing, fix wait
- `DEVICE_INFRASTRUCTURE` - Device/ADB/Appium issue
- `TEST_DATA` - Data/setup issue
- `AUTOMATION_DEFECT` - Code bug in framework

Only repair `LOCATOR_DRIFT`, `SYNC_ISSUE`, `AUTOMATION_DEFECT`.

### DOCUMENTATION UPDATES (Required)

After any automation work:
- `docs/exploration/SCREEN_CATALOG.md` - New screens discovered
- `docs/flows/FLOW_CATALOG.md` - New flows automated
- `docs/coverage/COVERAGE_MATRIX.md` - Coverage status
- `docs/automation/KNOWN_BLOCKERS.md` - Blockers found
- `docs/decisions/ARCHITECTURE_DECISIONS.md` - Architectural choices

### COMMANDS AGENTS SHOULD SUPPORT

```
Continue Android automation from verified state.
Explore the next uncovered Customer flow.
Automate the next Provider flow.
Run regression and repair automation defects only.
Audit current coverage.
Run gated CARD_SETUP only.
```

### FORBIDDEN PATTERNS

- `Thread.sleep()` anywhere
- Hardcoded device UDIDs in Java code
- Direct ExtentReports calls in tests
- Coordinate-based taps
- Empty placeholder tests
- Suppressing exceptions
- Changing expected values to match actuals
- Running gated tests without authorization
- Two agents controlling same device

### ENVIRONMENT VARIABLES (Required)

```bash
ANDROID_DEVICE_UDID=
ANDROID_DEVICE_NAME=
ANDROID_APP_PACKAGE=
ANDROID_APP_ACTIVITY=
CUSTOMER_PWA_URL=
ADMIN_PWA_URL=
AUTOMATION_OWNER_ID=antigravity
RUN_GATED_TESTS=false
GATED_CASE=
```

### REPOSITORY STRUCTURE

```
car-comfort-automation/
├── AGENTS.md                    # This file
├── pom.xml
├── .env.example
├── src/test/java/com/carcomfort/
│   ├── core/                    # Framework core
│   ├── mobile/android/          # Android screens/components
│   ├── mobile/ios/              # iOS screens/components (future)
│   ├── web/customer/            # Customer PWA
│   ├── web/admin/               # Admin PWA
│   ├── flows/                   # Business flows
│   └── tests/                   # Test classes
├── src/test/resources/
│   ├── config/                  # HOCON configs
│   ├── testdata/                # YAML test data
│   └── suites/                  # TestNG suites
├── docs/                        # Documentation
├── scripts/                     # Utility scripts
└── artifacts/                   # Generated: logs, reports, evidence, locks
```

### VERIFICATION CHECKLIST (Before declaring done)

- [ ] `mvn clean test -DsuiteXmlFile=src/test/resources/suites/smoke.xml` passes
- [ ] Physical Android device recognized
- [ ] Appium session creates successfully
- [ ] Car Comfort app launches
- [ ] First safe scenario executes
- [ ] Screenshots captured
- [ ] Logs structured
- [ ] Extent HTML report generated
- [ ] PDF report generated
- [ ] Failure evidence captured
- [ ] Gated protection blocks unauthorized
- [ ] AGENTS.md exists
- [ ] Skills in `.agents/skills/`
- [ ] Coverage/state files exist