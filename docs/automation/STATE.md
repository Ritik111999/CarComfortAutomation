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

## Coverage snapshot (mapping run 2026-09-22)
- Discovered screens: 11 unique nodes (`docs/exploration/ANDROID_SCREEN_GRAPH.json`, 12 edges)
  - Provider (8): home, bookings, booking detail top + route plan, wallet (Stripe gate), settings top + legal/logout, help
  - Shared (1): login (13 descs incl. Remember me, Forgot Password, social, Terms, Sign up GATED)
  - Customer (2): vehicle gate + license gate (both ACCOUNT_BOOTSTRAP/VERIFICATION GATED — B-002)
- Automated flows: 2 (CustomerAuthFlow, ProviderAuthFlow — prior verified, reused, not re-executed as tests this run)
- New Screen Objects: ProviderBookingsScreen, ProviderBookingDetailScreen, ProviderWalletScreen, ProviderHelpScreen; ProviderSettingsScreen extended (legal rows)
- Verified tests this run: mapping probes only (5 green runs, 0 product assertions) — no regression claimed
- Fingerprint dedup: home self-loop + bookings/wallet/settings multi-route reconciled (sorted content-desc sets)
- Blockers: B-001 resolved; B-002 OPEN (customer onboarding gate); B-003 info (weak semantics workaround verified)
- Incidents: BACK-from-tab-root exits app (Router Setup foregrounded, no interaction, dumps deleted, rule recorded); final device state below

## Physical device (updated 2026-09-22 — mapping run end state)
- Status: ATTACHED, `device` (authorized), USB transport
- UDID: c68e*** (masked; full value only in local env, never committed)
- Manufacturer/Model: Xiaomi 22021211RI (Redmi Note 11 / POCO M4 Pro 5G)
- Android 14 (SDK 34), 1080x2400, awake
- Car Comfort app: `io.carcomfort.app` v1.1.1 (versionCode 11), launcher `io.carcomfort.app/.MainActivity`
- Final app state: CUSTOMER onboarding gate (AND-CUST-LICENSE-001) — provider logged out cleanly (Logout+Okay verified 1x), customer logged in with env creds, vehicle/license bootstrap pending owner authorization. No gated CTA tapped at any point.
- Suggested env for exploration:
  `ANDROID_DEVICE_UDID=c68e*** ANDROID_DEVICE_NAME=22021211RI ANDROID_APP_PACKAGE=io.carcomfort.app ANDROID_APP_ACTIVITY=.MainActivity AUTOMATION_OWNER_ID=<agent>` (full UDID in local .env only)

## Next recommended action
1. Owner decision on B-002: authorize customer vehicle+license bootstrap (gated CASE) OR provide an already-onboarded customer account — until then CUST-003..010 stay BLOCKED (no repeated attempts).
2. Next provider session: re-verify `ProviderAuthFlow.openProfile()` (VERIFIED DRIVER) + traverse one deferred legal row (e.g. Privacy Policy, read-only) using shortest safe paths in NAVIGATION_MAP.
3. Do NOT run full regression until B-002 clears; targeted re-runs only (affected screen → module → smoke).
