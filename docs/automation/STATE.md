# Car Comfort Automation — State (mutable project memory)

> `AGENTS.md` holds permanent rules. This file holds current, changeable state.
> Every agent: read this + COVERAGE_MATRIX + SCREEN_CATALOG before modifying automation.

## Last verified
- Date (UTC): 2026-09-23
- Branch: main
- Framework compiles: YES (`mvn test-compile` green)
- Functional Lifecycle run: GREEN — `BookingLifecycleE2ETest.testBookingLifecycleHappyPath` PASSED (Build Success)
- Cross-role Lifecycle verified: `CC-E2E-ACCEPT-001` (Booking `#CC-CW-20260923-000006`, Car Wash, $35.35)
  - Phase 1: Customer booking creation + My Bookings detail verification (Route Plan, service notes, Confirmed, Unassigned)
  - Phase 2: Provider on-demand dispatch receipt correlated on Home Map
  - Phase 3: Provider acceptance executed once (15-min countdown timer started)
  - Phase 4: Customer cross-role synchronization verified in My Bookings
  - Phase 5: Provider service progression started -> "Enroute to pick up customer vehicle" -> "Arrived at service pickup location"
  - Prerequisite Boundary: Safely halted before OTP PIN verification and Before-Service physical camera photo upload.
- Extent HTML: YES (`artifacts/reports/extent/ExtentReport_exec-local-20260923-130411_20260923_130416.html`)
- PDF report: YES (`artifacts/reports/pdf/CarComfort_QA_Report_exec-local-20260923-130411_20260923_130416.pdf`)
- Physical device: Xiaomi 22021211RI, Android 14, USB, UDID c68e*** (masked); Car Comfort `io.carcomfort.app` v1.1.1.

## Environment (2026-09-22 audit)
- Java 21.0.10 (Temurin), Maven 3.9.15, Node v25.9.0
- Appium 3.3.0, UiAutomator2 7.1.2, XCUITest 11.0.0
- ANDROID_HOME=/Users/ritik/Library/Android/sdk, ADB present, 1 authorized device (c68e***)
- Appium MCP 1.94.3 + @appium/mcp-documentation 1.0.14 installed globally; workspace config `.agents/mcp_config.json` (NO_UI, docs, evidence ON; vision OFF)

## Coverage snapshot (2026-09-22 — PROVIDER SMOKE GREEN)
- Provider smoke: `ProviderLoginSmokeTest` + `ProviderNavigationSmokeTest` GREEN 2/2 in one device session (102.9s)
  - Exec: 2026-09-22 ~14:44–14:46 UTC+5:30; device 22021211RI Android 14; app io.carcomfort.app v1.1.1
  - Sequence verified: login → home (greeting poll) → profile (badge+onboarding) → BACK → bookings (filter+cards) → detail (read-only meta) → BACK → home-tab → wallet (live balances view-only) → settings (payment row, no entry) → help (read-only) → settings → logout → session-end
  - Backend variation observed: wallet Stripe gate GONE (verification completed outside automation); live balance/pending/payment-history rows render async — flow waits for body, asserts view-only, amounts masked in docs, zero financial taps
  - Logout contract: provider settings-logout lands on LOGIN form OR ROLE SELECTION (back-stack-dependent; evidence 20260922_144319_706) — either-marker fix, same lesson as customer; provider flow keeps its own contract
  - Repairs (automation-only): `isAuthenticated` absence-poll (login-title linger race, evidence 20260922_143723_361), greeting 15s poll (suite-sequential slow auth), wallet async-body wait; `ProviderBookingsScreen.isFilterAllShown`, `ProviderHelpScreen.isHelpContentShown` helpers
  - Cross-role check: `CustomerLoginSmokeTest` GREEN after provider work (customer→provider→customer isolation holds; no shared-code changes — BottomNavComponent/base untouched)
  - Reports: fresh Extent/PDF from this run (not Customer reports)
- Customer smoke retained GREEN (2/2 earlier same day; untouched this turn — zero customer files modified).
- Customer smoke: `CustomerLoginSmokeTest` + `CustomerNavigationSmokeTest` GREEN 2/2 in one device session (89.46s)
  - Exec: 2026-09-22 ~14:27–14:29 UTC+5:30; device 22021211RI Android 14; app io.carcomfort.app v1.1.1
  - Sequence verified: login → home (greeting + cards) → Car Wash wizard step 1 (Next untouched) → bookings (filter + cards) → active (empty state) → settings (core rows, no gated entry) → support (read-only) → profile (payment row, PII masked) → logout → login form
  - Reports: `artifacts/reports/extent/ExtentReport_exec-local-20260922-142753_*.html`, `artifacts/reports/pdf/CarComfort_QA_Report_exec-local-20260922-142753_*.pdf`
- Logout contract fix: `CustomerAuthFlow.logout()` first overfit to login form (settings-path evidence), failed on profile path (role selection); corrected to accept EITHER marker (back-stack-dependent product behavior, 2 evidences); `CustomerNavigationFlow.toLoginFromRoleSelection()` completes to login from either state. Provider flow untouched.
- Automation repairs: `BaseAndroidComponent` root-less constructor (new overload; `/hierarchy` root lookup rejected by UiAutomator2); `BottomNavComponent` bounds-based tabs; support/settings/wizard presence helpers; `CustomerHomeScreen.tapCombo()`.
- Previous mapping snapshot retained below (21 nodes / 22 edges; provider reused, zero re-exploration; no gated CTA tapped).
- Discovered screens: 21 unique nodes (`docs/exploration/ANDROID_SCREEN_GRAPH.json`, 22 edges)
  - Provider (8): home, bookings, booking detail top + route plan, wallet (Stripe gate), settings top + legal/logout, help — REUSED, not re-explored
  - Shared (1): login (13 descs)
  - Customer live (10): home, 3 service-wizard Location steps, bookings (Confirmed/Completed/Cancelled), active-booking empty state, settings top + legal/logout, support, profile (13 rows, PII masked)
  - Historical gates (2): vehicle + license onboarding (transient, excluded from normal paths)
- Role determined: CUSTOMER ("Carl Customer!" greeting; owner "provider" label corrected with evidence)
- Cancel Signup: owner-authorized one-time — NOT EXECUTED (cancelTaps=0; gate absent at session start; auth recorded unused, never auto-run)
- B-002: RESOLVED (transient gate cleared; relogin → home verified)
- ONBOARDING_RESET_COMPLETED: NOT_REQUIRED (no reset executed; skip onboarding path when home login works)
- New Screen Objects: CustomerBookingsScreen, CustomerActiveBookingScreen, CustomerSettingsScreen, CustomerSupportScreen, CustomerServiceWizardScreen; CustomerProfileScreen re-verified notes
- Findings: customer Log Out+Okay lands on LOGIN form (CustomerAuthFlow.logout expects role-select — flagged, code untouched); deferred safe edges: customer bell, wizard steps 2-4 + Next, customer booking detail, Payment/Billing/Switch entries
- Verified tests this run: 3 green mapping probes (state probe, reset+home+carwash attempt, full customer map), 0 product assertions — no regression claimed
- Fingerprint dedup: customer tab0 self-loop + bookings/support multi-route reconciled
- Final device state: customer logged OUT (authorized teardown → login form); app foreground io.carcomfort.app; no gated CTA tapped at any point (Cancel taps=0)

## Physical device (updated 2026-09-22 — mapping run end state)
- Status: ATTACHED, `device` (authorized), USB transport
- UDID: c68e*** (masked; full value only in local env, never committed)
- Manufacturer/Model: Xiaomi 22021211RI (Redmi Note 11 / POCO M4 Pro 5G)
- Android 14 (SDK 34), 1080x2400, awake
- Car Comfort app: `io.carcomfort.app` v1.1.1 (versionCode 11), launcher `io.carcomfort.app/.MainActivity`
- Final app state: customer LOGGED OUT (authorized teardown; login form foreground). Prior run ended at customer license gate; this run: gate absent, relogin → home, full customer mapping, logout. Cancel Signup taps=0. No gated CTA tapped at any point.
- Suggested env for exploration:
  `ANDROID_DEVICE_UDID=c68e*** ANDROID_DEVICE_NAME=22021211RI ANDROID_APP_PACKAGE=io.carcomfort.app ANDROID_APP_ACTIVITY=.MainActivity AUTOMATION_OWNER_ID=<agent>` (full UDID in local .env only)

## Next recommended action
1. Targeted automation: extend `CustomerAuthFlow` (bookings/active/settings/support/profile navigation) + fix logout destination note (lands on LOGIN form) — owner-reviewed, then run Customer smoke only (affected flow → customer smoke; no full regression).
2. Deferred safe edges when scheduled: customer bell, wizard steps 2-4 (needs submission-boundary scoping before Next), customer booking detail, legal-row details.
3. Do NOT re-run Cancel Signup (auth consumed/unused; never automatic) and do NOT re-explore provider (8 screens reused).
