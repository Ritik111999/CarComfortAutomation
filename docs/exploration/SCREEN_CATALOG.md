# Car Comfort Screen Catalog

> Machine source: `docs/exploration/ANDROID_SCREEN_GRAPH.json` (11 nodes, 12 edges, app v1.1.1, 22021211RI Android 14, 2026-09-22).
> Evidence: git-ignored `artifacts/evidence/mapping/*.xml|*.png`. PII: street/shop/plot masked in docs; full hierarchy only in artifacts.
> Prior modelled screens (RoleSelection, Login, CustomerHome/Profile, ProviderProfile) reuse verified 2026-09-22 knowledge — NOT rediscovered (checkpoint reuse).
>
> ## Smoke verification (2026-09-22 — CustomerNavigationSmokeTest + CustomerLoginSmokeTest GREEN 2/2)
> VERIFIED on device: AND-CUST-HOME-001, AND-CUST-SVC-CARWASH-001 (step 1; EV/Combo mapped), AND-CUST-BOOKINGS-001, AND-CUST-ACTIVE-001, AND-CUST-SETTINGS-001/002, AND-CUST-SUPPORT-001, AND-CUST-PROFILE-001, AND-SHARED-LOGIN-001 (post-logout). Mapping-only (not in smoke): EV/Combo wizard execution, customer bell, wizard steps 2-4, customer booking detail.

## Shared

### AND-SHARED-LOGIN-001 — Login ("Welcome to Car Comfort")
- role: SHARED (Customer + Provider, role pre-selected before form)
- module: auth
- parent/entry: role selection ("Select your role") → select role; provider logout returns here directly
- stable locators (HIGH unless noted): accessibilityId `Welcome to Car Comfort` (unique), `Login`, `Forgot Password?`, `Remember me`, `Sign up now` (GATED, never tap), `Terms and Conditions`, `Privacy Policy`; EditTexts: UiAutomator `EditText.instance(0)` email / `instance(1)` password (MEDIUM — no hint/desc, weak Flutter semantics, filed for app improvement)
- safe actions: enter email/password (focus-tap + BACK-dismiss autofill overlay, never tap autofill), tap Login, tap Forgot Password? (view only — not traversed)
- outgoing: → AND-PROV-HOME-001 (provider creds) | → AND-CUST-HOME-001 (customer creds; transient vehicle/license gate observed in one prior session, cleared — Cancel taps=0)
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

## Customer (verified CUSTOMER role: greeting "Carl Customer!" — owner "provider" label was inaccurate)

### AND-CUST-HOME-001 — Customer Home (map + "Enter Service Details" sheet)
- role: CUSTOMER (proven by greeting + customer bookings cross-match) | module: dashboard | parent: login submit
- fingerprint: {`Carl Customer!` + `Enter Service Details` + `Car Wash` + `EV Charging` + `EV charging & Car wash` + `Powered by Mapbox Maps`}
- locators: service cards accessibilityId HIGH; avatar `ImageView.instance(3)` MEDIUM; bottom tabs 5 × y≈2241–2362 MEDIUM; bell top-right MEDIUM (untapped — unexplored safe edge)
- safe actions: tap 3 service cards (wizard step 1 each), tap tabs 1/2/3/4, tap avatar, tap bell (deferred)
- outgoing: → AND-CUST-SVC-CARWASH/EVCHARGING/COMBO-001 | → AND-CUST-BOOKINGS-001 (tab1) | → AND-CUST-ACTIVE-001 (tab2) | → AND-CUST-SETTINGS-001 (tab3) | → AND-CUST-SUPPORT-001 (tab4) | → AND-CUST-PROFILE-001 (avatar)
- mapping: MAPPED + MODELLED (`CustomerHomeScreen.java` prior + re-verified) | last verified: 2026-09-22 (3 routes)

### AND-CUST-SVC-CARWASH-001 — Car Wash wizard, step 1 Location
- stable: `Enter Car Wash Service Details`, steps 1-4 `Location/Service/Vehicle/Review`, `Location Details`, `Customer Location`, `Or Enter Address Manually`, `Location services are disabled.`, `Can't Find Service Location? Enter It Here`, CTA `Next: Car Wash Details`
- safe boundary: STOP before `Next` (advances toward GATED submission)
- mapping: MAPPED | MODELLED (new `CustomerServiceWizardScreen.java`) | edge BACK → home (pushed, safe)

### AND-CUST-SVC-EVCHARGING-001 — EV Charging wizard, step 1 Location
- stable: `Enter EV Charging Service Details`, steps `Location/Charging/Vehicle/Review`, `Select Charging Type`, `Tesla Supercharger`, `We'll show you available charging stations near your location`, `Do you have an EV account with a card on file for charging purposes?`, `YES`/`NO` (presence-only — option selection deferred as pre-submission state), CTA `Next: Charging Details`
- safe boundary: STOP before `Next`; YES/NO left untouched (wizard state, submission path GATED downstream)
- mapping: MAPPED | last verified: 2026-09-22

### AND-CUST-SVC-COMBO-001 — Combo wizard, step 1 Location
- stable: `Combined Service Details`, steps `Location/Service/Vehicle/Review`, `Combo`, `Your service provider will visit the selected charging station first, then head to the car wash location, before heading towards the drop off location`, CTA `Next: Service Details`
- mapping: MAPPED | last verified: 2026-09-22

### AND-CUST-BOOKINGS-001 — Customer My Bookings
- stable: `My Bookings`, `All`, 3 cards (all `[address masked] Nagpur`): Confirmed `EV charging & Car wash / Service Provider Not Assigned / $212.68 / Sep 17 2026` + 2 Completed `3.0 karl Driver / $212.68 / Sep 15`, scrolled 4th `Cancelled by service provider / Cancelled / $155.69 / Sep 11`; each `View Service Details`
- cross-role: Completed cards match provider booking #CC-CB-20260915-000013 value class ($124.99 provider payout vs $212.68 customer charge — relationship noted, no lifecycle executed)
- safe: detail entry deferred (provider detail already maps the pattern); cancellation actions GATED by policy
- mapping: MAPPED | MODELLED (new `CustomerBookingsScreen.java`) | last verified: 2026-09-22 (2 routes)

### AND-CUST-ACTIVE-001 — My Active Booking (empty state)
- stable: `My Active Booking`, `No Active Booking`, `You don't have any active or upcoming bookings right now.`, `Start a New Booking` (presence-only)
- mapping: MAPPED | MODELLED (new `CustomerActiveBookingScreen.java`) | last verified: 2026-09-22

### AND-CUST-SETTINGS-001 — Customer Settings (top)
- stable (HIGH): `Settings`, `Switch to Service Provider` (NOT tapped), `Payment Methods` (GATED), `Booking History`, `Billing History`, `Notifications`, `Refer & Earn`, `Tutorial`, `File a Claim`, `Report Accident/Damage`, `Report Missing Item` (all presence-only)
- outgoing: → AND-CUST-SETTINGS-002 (scroll)
- mapping: MAPPED | MODELLED (new `CustomerSettingsScreen.java`) | last verified: 2026-09-22

### AND-CUST-SETTINGS-002 — Customer Settings (legal + logout)
- stable: `Terms & Conditions`, `Privacy Policy`, `Waiver`, `Support`, `Delete Account` (DESTRUCTIVE — never tapped), `Log Out` (authorized teardown → LOGIN form, verified)
- edge: → AND-SHARED-LOGIN-001 via `Log Out`+`Okay`
- mapping: MAPPED | last verified: 2026-09-22

### AND-CUST-SUPPORT-001 — Customer Support (tab4)
- stable: `Support`, `How we can help you?`, customer help text, scrolled `Email Us`, `Chat With Us`, `Call Support 1-800-702-3590` (presence-only, no intents fired)
- shared-vs-role: same component pattern as AND-PROV-HELP-001, role-specific copy (SHARED_UI=false — separate objects)
- mapping: MAPPED | MODELLED (new `CustomerSupportScreen.java`) | last verified: 2026-09-22 (2 routes)

### AND-CUST-PROFILE-001 — Customer Profile (avatar)
- stable (13 nodes, row names only — values masked): `Profile`, referral block, `Account`, `Personal Information`, `My Vehicles 3` (answers Outcome B: vehicle management lives here as normal path — prior vehicle screen content reusable as component, no ID duplication), `Payment Methods` (GATED), `Log Out`
- edge BACK → home (pushed, safe)
- mapping: MAPPED + MODELLED (`CustomerProfileScreen.java` re-verified) | last verified: 2026-09-22

### AND-CUST-VEHICLE-001 / AND-CUST-LICENSE-001 — reclassified: transient incomplete-signup gates (Outcome A)
- role: CUSTOMER (proven: appeared post-customer-login; owner "provider" label inaccurate — same wizard mechanism may exist per-role but observed only here)
- status: state-dependent onboarding screens, NOT normal authenticated navigation; retained in graph/catalog as gate record, excluded from normal paths
- Cancel Signup: owner-authorized one-time exit — NOT EXECUTED (cancelTaps=0; gate cleared transiently before action needed; auth recorded as unused). Classification stays GATED_ONBOARDING_EXIT (controlled, non-regression, never auto-run).

### Customer logout destinations — VERIFIED PER EVIDENCE (code corrected 2026-09-22)
- Profile `Log Out`+`Okay` → logged-out auth area with a BACK-STACK-DEPENDENT destination: ROLE SELECTION (evidence 20260922_142209_041) or LOGIN form directly (evidence 20260922_142614_576). `CustomerAuthFlow.logout()` accepts either marker (no overfit); `CustomerNavigationFlow.toLoginFromRoleSelection()` completes to the login form from either state.
- Settings-tab `Log Out`+`Okay` → LOGIN form (mapping evidence AND-SHARED-ROLE-010).

### AND-CUST-VEHICLE-001 — (superseded detail; see reclassification above)
- prior record: `Cancel Signup`, `Add Vehicle Information`, Make/Model/year/color/plate/fuel, `Save Vehicle` — all GATED, never tapped; gate cleared transiently, Cancel taps=0
- status: HISTORICAL gate record only

### AND-CUST-LICENSE-001 — (superseded detail; see reclassification above)
- prior record: `Driver License`, Upload Front/Back (VERIFICATION, never tapped)
- status: HISTORICAL gate record only

### Prior-model note (fulfilled)
- `CustomerHomeScreen`, `CustomerProfileScreen`, `CustomerAuthFlow` — all RE-VERIFIED live this run (home greeting, profile rows, logout path observed); prior checkpoint reuse honored (no redundant rediscovery).

## Shared-UI
- SHARED_UI=true: Login form layout, bottom-tab bar pattern (5 tabs), Settings row pattern, Help page pattern, logout `Okay` confirm dialog. Reused via `BaseAndroidScreen` + `LocatorFactory`; role-specific screens stay separate.

## Locator confidence summary
- HIGH: all accessibilityId content-desc screens/rows/cards/buttons above (primary strategy; stop searching)
- MEDIUM: avatar/bottom-nav/bell/floating buttons (positional UiAutomator instances — per-screen numbering, always re-query by bounds, never hardcode across screens); email/password EditText instances (weak semantics workaround)
- LOW: none adopted. BLOCKED: coordinates (never used); email/password hint/desc (app-improvement request).
- Duplicates prevented: home self-loop (tab0), bookings/wallet/settings multi-route (fingerprint dedup on sorted content-desc sets).

## Automation blockers
- B-002: RESOLVED 2026-09-22 (transient incomplete-signup gate cleared without Cancel; relogin → home verified; Cancel taps=0, auth unused; historical record retained).
- B-003 (info): Flutter weak semantics on email/password/avatar/nav (no desc/id) — positional workaround verified; app-improvement: add content-desc/hint.
- App-boundary incident (resolved): BACK from tab root exits app (verified — Router Setup app briefly foregrounded, no interaction, immediately returned; Router dumps deleted, excluded from graph). Rule: BACK only from pushed screens; tab returns via home-tab tap.
