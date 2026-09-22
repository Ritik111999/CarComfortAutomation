# Car Comfort Automation — State (mutable project memory)

> `AGENTS.md` holds permanent rules. This file holds current, changeable state.
> Every agent: read this + COVERAGE_MATRIX + SCREEN_CATALOG before modifying automation.

## Last verified
- Date (UTC): 2026-09-22
- Branch: main
- Framework compiles: YES (`mvn test-compile` green)
- Smoke run: GREEN — `mvn clean test` → latest run Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
  (device probe now passes with hardware attached; earlier no-device run: 8 run, 1 clean skip)
- Extent HTML: YES (`artifacts/reports/extent/ExtentReport_exec-local-*`)
- PDF report: YES (`artifacts/reports/pdf/CarComfort_QA_Report_exec-local-*`)
- Failure evidence path: wired via EvidenceCollector in BaseTest (no failures to demonstrate yet)
- Gated protection: VERIFIED — `gated-one-time.xml` runs 0 tests without authorization;
  `FrameworkSafetyTest.testGatedGuardBlocksWithoutAuthorization` proves guard aborts without
  RUN_GATED_TESTS=true + GATED_CASE
- Physical device: ATTACHED 2026-09-22 — Xiaomi 22021211RI, Android 14, USB, UDID c68e*** (masked);
  Car Comfort `io.carcomfort.app` v1.1.1, `.MainActivity`. See "Physical device" section below.

## Environment (2026-09-22 audit)
- Java 21.0.10 (Temurin), Maven 3.9.15, Node v25.9.0
- Appium 3.3.0, UiAutomator2 7.1.2, XCUITest 11.0.0
- ANDROID_HOME=/Users/ritik/Library/Android/sdk, ADB present, 1 authorized device (c68e***)
- Appium MCP 1.94.3 + @appium/mcp-documentation 1.0.14 installed globally; workspace config `.agents/mcp_config.json` (NO_UI, docs, evidence ON; vision OFF)

## Coverage snapshot
- Discovered screens: 0 (device present; exploration pending owner confirmation)
- Automated flows: 0 (framework smoke only)
- Verified tests: 8 framework-level passed (0 product tests on device yet)
- Blockers: B-001 device leg resolved; awaiting sanction to drive UI

## Physical device (updated 2026-09-22 — unit attached mid-task, read-only fingerprinted)
- Status: ATTACHED, `device` (authorized), USB transport
- UDID: c68e*** (masked; full value only in local env, never committed)
- Manufacturer/Model: Xiaomi 22021211RI (Redmi Note 11 / POCO M4 Pro 5G)
- Android 14 (SDK 34), 1080x2400, awake
- Car Comfort app: `io.carcomfort.app` v1.1.1 (versionCode 11), launcher `io.carcomfort.app/.MainActivity`
- B-001: device leg RESOLVED — discovery unblocked. No Appium session created yet (see next action).
- Suggested env for exploration:
  `ANDROID_DEVICE_UDID=c68eec8e ANDROID_DEVICE_NAME=22021211RI ANDROID_APP_PACKAGE=io.carcomfort.app ANDROID_APP_ACTIVITY=.MainActivity AUTOMATION_OWNER_ID=<agent>`

## Next recommended action
1. Owner confirms c68e*** is the sanctioned test device (it presents as a personal phone — no UI-driving without confirmation).
2. Export the env above, acquire the device lock, and open the first safe Appium MCP exploratory session
   (NO_UI=true, safe mode: hierarchy + screenshots, no logins/submits, no gated flows).
3. Record the first SCREEN_CATALOG entries + mark CUST-001 DISCOVERED.
