# Car Comfort Screen Catalog

> Machine source: `docs/exploration/ANDROID_SCREEN_GRAPH.json` (11 nodes, 12 edges, app v1.1.1, 22021211RI Android 14, 2026-09-22).
> Evidence: git-ignored `artifacts/evidence/mapping/*.xml|*.png`. PII: street/shop/plot masked in docs; full hierarchy only in artifacts.
> Prior modelled screens (RoleSelection, Login, CustomerHome/Profile, ProviderProfile) reuse verified 2026-09-22 knowledge — NOT rediscovered (checkpoint reuse).

## Shared

### AND-SHARED-LOGIN-001 — Login ("Welcome to Car Comfort")
- role: SHARED (Customer + Provider, role pre-selected before form)
- module: auth
- parent/entry: role selection ("Select your role") → select role; provider logout returns here directly
- stable locators (HIGH unless noted): accessibilityId `Welcome to Car Comfort` (unique), `Login`, `Forgot Password?`, `Remember me`, `Sign up now` (GATED, never tap), `Terms and Conditions`, `Privacy Policy`; EditTexts: UiAutomator `EditText.instance(0)` email / `instance(1)` password (MEDIUM — no hint/desc, weak Flutter semantics, filed for app improvement)
- safe actions: enter email/password (focus-tap + BACK-dismiss autofill overlay, never tap autofill), tap Login, tap Forgot Password? (view only — not traversed)
- outgoing: → AND-PROV-HOME-001 (provider creds) | → AND-CUST-VEHICLE-001 (customer creds, onboarding gate)
- gated: `Sign up now` (account creation — GATED/ONE_TIME, never tapped)
- blockers: email/password fields expose no hint/content-desc/resource-id (AUTOMATION_BLOCKER filed as app-improvement request; workaround: class-instance focus-tap verified)
- mapping: MAPPED + MODELLED (`LoginScreen.java`) | automation: AUTOMATED (smoke) | last verified: 2026-09-22

### AND-SHARED-ROLE-001 — Role Selection ("Select your role") (from code, prior verified)
- role: SHARED | module: auth
- locators: accessibilityId `Select your role`, `Customer`, `Service Provider` (all HIGH)
- outgoing: → login (per role)
- mapping: MODELLED (`RoleSelectionScreen.java`) | last verified: 2026-09-22 (prior run; not re-driven — checkpoint reuse)

## Provider (karl Driver, fully onboarded: VERIFIED DRIVER, 8 steps complete per prior model)

### AND-PROV-HOME-001 — Provider Home (map + greeting)
- module: dashboard | parent: login restore / tab0
- fingerprint: {`Good Afternoon,` + `karl Driver!` + `Powered by Mapbox Maps`} (time prefix varies; `Driver!` suffix stable)
- locators: avatar `ImageView.instance(3)` (MEDIUM positional, no desc); bottom tabs = 5 clickable nodes at y≈2241–2362 (tab0..tab4 left→right, MEDIUM positional); bell `View[937,139]` (MEDIUM); greeting `descriptionContains("Driver!")` (HIGH)
- safe actions: tap tab0..tab4, tap bell, tap avatar (→ profile, prior model), tap greeting
- outgoing: → AND-PROV-BOOKINGS-001 (tab1) | → AND-PROV-WALLET-001 (tab2) | → AND-PROV-SETTINGS-001 (tab3) | → AND-PROV-HELP-001 (tab4)
- side effects: none (tab switches read-only)
- mapping: MAPPED + MODELLED (`ProviderHomeScreen.java`) | last verified: 2026-09-22 (4 routes, dup reconciled)

### AND-PROV-BOOKINGS-001 — My Bookings
- module: bookings | parent: home tab1
- stable: accessibilityId `My Bookings`, `All` (filter), `View Service Details` ×2; cards: `Carl Customer / EV charging & Car wash / Completed / payout $124.99 / 0.9 miles / Scheduled Sep 15 2026` ×2 ([address masked], Nagpur, India)
- safe actions: tap View Service Details (pushed detail), filter `All` presence (other filters not observed — conditional)
- outgoing: → AND-PROV-REQDETAIL-001
- gated: Accept/Reject/Complete/Cancel NOT present on Completed cards (no side-effect risk observed); pending-job actions remain GATED by policy (never tapped if seen)
- mapping: MAPPED | automation: MODELLED (new `ProviderBookingsScreen.java`) | last verified: 2026-09-22 (3 routes)

### AND-PROV-REQDETAIL-001 — Booking Detail (top)
- module: bookings | parent: bookings → View Service Details (pushed; BACK pops — safe)
- stable (all HIGH content-desc): `Service Details`, `Carl Customer`, `BOOKING ID #CC-CB-20260915-000013`, `BOOKING STATUS Completed`, `CURRENT STATUS …after-service photos and final billing…`, `Service details`, `Service type – EV charging and Car wash`, `Membership Package`, `Premium car wash`, `Select option #4 on car wash menu`, `EV account with card on file – No`, `Current battery – 9%`, `Vehicle charge limit – 50%`, `Time of service – As soon as possible`, `Service provider earnings – $124.99`
- outgoing: → AND-PROV-REQDETAIL-002 (scroll) | BACK → bookings
- gated: none visible on Completed detail (no action buttons rendered)
- mapping: MAPPED | MODELLED (new `ProviderBookingDetailScreen.java`) | last verified: 2026-09-22

### AND-PROV-REQDETAIL-002 — Booking Detail (route plan, scrolled)
- stable: `Estimated service duration – 70min`, `ESTIMATED RADIUS 3.7 miles`, `Route Plan`, `PICKUP [road masked] Nagpur 440022`, `STOP 1 · EV CHARGING [station masked]`, `Charge limit: 50%`, `STOP 2 · CAR WASH [shop masked]`, `Package: Premium car wash`, `RETURN DROPOFF [road masked]`
- mapping: MAPPED | last verified: 2026-09-22

### AND-PROV-WALLET-001 — My Wallet (Stripe-gated)
- module: earnings | parent: home tab2
- stable: `My Wallet`, `Complete Stripe verification`, `Complete Stripe verification to access your wallet and withdraw earnings.`, `Complete verification`, `I've completed verification — refresh`
- safe actions: NONE beyond viewing (all CTAs lead to Stripe onboarding/verification)
- gated: `Complete verification` (Stripe onboarding/verification — PAYMENT_SETUP/VERIFICATION, GATED_CASE required; never tapped)
- mapping: MAPPED | MODELLED (new `ProviderWalletScreen.java`, presence-only) | last verified: 2026-09-22

### AND-PROV-SETTINGS-001 — Provider Settings (top)
- module: settings | parent: home tab3
- stable (HIGH): `Settings`, `Switch to customer` (NOT tapped — role-switch side effects need owner scoping), `Profile Settings` (presence only — PII inside), `Payment Methods` (GATED entry), `Wallet` (→ wallet, not traversed — already mapped via tab), `Booking History` (presence only — same list as My Bookings, not re-entered), `Tutorials`, `Learning Center`, `File a Claim` (presence only), `Report Accident/Damage`, `Report Missing Item`
- outgoing: → AND-PROV-SETTINGS-002 (scroll)
- mapping: MAPPED | MODELLED (`ProviderSettingsScreen.java` extended) | last verified: 2026-09-22 (3 routes)

### AND-PROV-SETTINGS-002 — Provider Settings (legal + logout)
- stable: `Earnings Statement (PDF)` (presence only — not opened), `Notifications` (presence only), `Waiver`, `Service Provider NDA`, `Independent Contractor Agreement`, `Privacy Policy`, `Vehicle User Agreement`, `Terms & Conditions` (all presence-only informational candidates), `Support`, `Delete Account` (DESTRUCTIVE — never tapped), `Logout` (authorized teardown → login)
- edge: → AND-SHARED-LOGIN-001 via `Logout`+`Okay` (1x role transition, verified 2026-09-22)
- mapping: MAPPED | last verified: 2026-09-22

### AND-PROV-HELP-001 — Help / Support (tab4)
- module: support | parent: home tab4
- stable: `Need Help?`, `How we can help you?`, provider terms (onboarding, payouts, active jobs, account, safety; U.S. business hours)
- safe: read-only informational
- mapping: MAPPED | MODELLED (new `ProviderHelpScreen.java`) | last verified: 2026-09-22

### AND-PROV-PROFILE-001 — Service Provider Profile (prior model, NOT re-driven)
- stable per verified code: `Service Provider Profile`, `VERIFIED DRIVER`, `All 8 steps complete`, expanders unopened (license/background PII), `Log Out` below fold + `Okay` confirm
- mapping: MODELLED (`ProviderProfileScreen.java`, `ProviderSettingsScreen.java`) | automation: AUTOMATED (provider smoke) | last verified: 2026-09-22 prior run
- note: run avatar taps in this session landed on Settings due to per-screen instance shift (lesson recorded); profile re-verification deferred to next provider session via `ProviderAuthFlow.openProfile()` (no new discovery needed)

## Customer (account in onboarding — home unreachable until bootstrap authorized)

### AND-CUST-VEHICLE-001 — Add Vehicle Information (ACCOUNT_BOOTSTRAP gate) ⛔
- role: CUSTOMER | module: onboarding | parent: login submit (authorized customer creds land here, not home)
- stable: `Cancel Signup` (DESTRUCTIVE candidate — never tapped), `Add Vehicle Information`, `Select Vehicle Type`, `Make *`, `Model *`, year/color/plate/fuel rows, `Save Vehicle` (creates vehicle record — GATED)
- safe actions: NONE (all CTAs mutate onboarding state)
- gated: `Save Vehicle` (ACCOUNT_BOOTSTRAP), `Cancel Signup` (DESTRUCTIVE/BOOTSTRAP — needs owner call)
- mapping: MAPPED (gate documented) | automation: BLOCKED | last verified: 2026-09-22
- unblocks: owner authorizes vehicle creation (gated CASE) or provides onboarded customer account → then CUST-003/004/005 mappable

### AND-CUST-LICENSE-001 — Driver License upload (VERIFICATION gate) ⛔
- entry: BACK from vehicle form (dismiss-only navigation, no save)
- stable: `Driver License`, `Upload front and back photos`, `license photo must match profile photo`, `Upload Front Image`, `Upload`, `Upload Back Image`
- gated: all Upload CTAs (identity verification + PII photos — VERIFICATION, never tapped)
- mapping: MAPPED (gate documented) | automation: BLOCKED | last verified: 2026-09-22

### Customer home/bookings/profile (prior model, NOT reachable this session)
- `CustomerHomeScreen` (`Enter Service Details`, `Car Wash`, `EV Charging`, combo; avatar/nav positional), `CustomerProfileScreen` (`Profile`, `Personal Information`, `My Vehicles`, `Payment Methods` GATED, `Log Out`+`Okay`), `CustomerAuthFlow` — all MODELLED/AUTOMATED from prior verified run; re-verification deferred until onboarding gate clears. No rediscovery attempted (checkpoint reuse).

## Shared-UI
- SHARED_UI=true: Login form layout, bottom-tab bar pattern (5 tabs), Settings row pattern, Help page pattern, logout `Okay` confirm dialog. Reused via `BaseAndroidScreen` + `LocatorFactory`; role-specific screens stay separate.

## Locator confidence summary
- HIGH: all accessibilityId content-desc screens/rows/cards/buttons above (primary strategy; stop searching)
- MEDIUM: avatar/bottom-nav/bell/floating buttons (positional UiAutomator instances — per-screen numbering, always re-query by bounds, never hardcode across screens); email/password EditText instances (weak semantics workaround)
- LOW: none adopted. BLOCKED: coordinates (never used); email/password hint/desc (app-improvement request).
- Duplicates prevented: home self-loop (tab0), bookings/wallet/settings multi-route (fingerprint dedup on sorted content-desc sets).

## Automation blockers
- B-002 (new): Customer onboarding gate (vehicle + license) blocks all CUST home flows — needs owner bootstrap authorization. Not a locator issue; no repeated attempts.
- B-003 (info): Flutter weak semantics on email/password/avatar/nav (no desc/id) — positional workaround verified; app-improvement: add content-desc/hint.
- App-boundary incident (resolved): BACK from tab root exits app (verified — Router Setup app briefly foregrounded, no interaction, immediately returned; Router dumps deleted, excluded from graph). Rule: BACK only from pushed screens; tab returns via home-tab tap.
