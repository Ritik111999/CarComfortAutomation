# Car Comfort Flow Catalog

## Business Flows

*To be populated during automation phase*

### Format
```
FLOW_ID:
  name:
  role:
  platform:
  description:
  preconditions:
  steps:
    - step:
      screen:
      action:
      verification:
  postconditions:
  test data:
  gated:
  automation status:
  test ID:
  notes:
```

---

## Customer Flows

### FLOW-CUST-AUTH-001 — Customer login → home (VERIFIED 2026-09-22)
- role: CUSTOMER | screens: AND-SHARED-LOGIN-001 → AND-CUST-HOME-001 (`Carl Customer!` + service cards)
- note: transient vehicle/license gate seen in one prior session only; logout destination back-stack-dependent (role-select OR login) — accepted either; completion to login via `CustomerNavigationFlow.toLoginFromRoleSelection()`. Status: AUTOMATED + VERIFIED (`CustomerLoginSmokeTest` GREEN).
### FLOW-CUST-BROWSE-001 — Service wizard inspection (VERIFIED 2026-09-22)
- steps: home → Car Wash / EV Charging / Combo card → Location step (4-step header, address/manual entry, service-specific options) → BACK; never taps Next (GATED_BUSINESS_ACTION boundary). Status: AUTOMATED + VERIFIED (`CustomerNavigationSmokeTest` GREEN; Car Wash path executed, EV/Combo mapped).
### FLOW-CUST-BOOKINGS-001 — View My Bookings + Active empty state (VERIFIED 2026-09-22)
- steps: home tab1 → bookings (Confirmed Not-Assigned + 2 Completed + scrolled Cancelled) → BACK/read; tab2 → active empty state. Status: AUTOMATED + VERIFIED (`CustomerNavigationSmokeTest` GREEN).
### FLOW-CUST-BOOK-001 — Customer Car Wash Booking Flow (VERIFIED 2026-09-23)
- role: CUSTOMER | screens: AND-CUST-HOME-001 → Car Wash Wizard (Location → Service → Vehicle → Review) → Booking Confirmed → My Bookings → View Service Details
- steps: Home → Car Wash → Enter address (Nagpur) + select suggestion → Select RAJMANI CAR WASH → Next → Membership 'I don't have a car wash membership' + CarComfort Package + ASAP → Next → Audi A5 vehicle + 4 parking/key questions → Next → Review summary + capture reviewTotal ($35.35) → Confirm Booking (1 tap) → Booking Confirmed (extract Booking ID #CC-CW-20260923-000006) → Home → My Bookings → Match exact booking card (Confirmed / Service Provider Not Assigned) → View Service Details (verify route plan, parking notes, price).
- status: AUTOMATED + VERIFIED (`BookingLifecycleE2ETest` GREEN on physical Xiaomi 22021211RI).

### FLOW-CUST-SETTINGS-001 — Settings review → authorized logout (VERIFIED 2026-09-22)
- steps: home tab3 → settings → scroll → Log Out + Okay → login form | status: AUTOMATED + VERIFIED (`CustomerNavigationSmokeTest` GREEN; teardown only)

---

## Provider Flows

### FLOW-PROV-AUTH-001 — Provider login → home → profile → settings → logout (VERIFIED 2026-09-22)
- role: PROVIDER | status: AUTOMATED + VERIFIED (`ProviderAuthFlow`, `ProviderLoginSmokeTest` GREEN; logout accepts login-form OR role-select markers — back-stack-dependent, same lesson as customer)
### FLOW-PROV-ACCEPT-001 — Provider on-demand job receipt & acceptance (VERIFIED 2026-09-23)
- role: PROVIDER | screens: AND-PROV-HOME-001 (interactive map) → On-demand Dispatch Bottom Sheet → Accepted trip state
- steps: Home Map displays on-demand dispatch pin (e.g. `45 min`) → Click pin to open bottom sheet (verifies Car Wash, venue BabulKheda Nagpur, payout $85.00) → Tap Accept once → Transitions to Accepted state with active 15-minute countdown timer ('14:52 Start driving to pick-up before the timer ends.')
- status: AUTOMATED + VERIFIED (`BookingLifecycleE2ETest` GREEN on physical Xiaomi 22021211RI).

### FLOW-PROV-PROGRESS-001 — Provider trip progression (VERIFIED 2026-09-23)
- role: PROVIDER | screens: Active trip view → Mapbox navigation → Arrival
- steps: Active trip view displays `Start` CTA → Tap `Start` once → Status transitions to `Enroute to pick up customer vehicle` with active Mapbox turn-by-turn navigation → Progression verb changes to `Arrived at service pickup location`.
- boundary: Progression past arrival requires physical camera photo upload ('Before Service Photos') and Customer OTP PIN verification. Safely stopped at boundary.
- status: AUTOMATED + VERIFIED (`BookingLifecycleE2ETest` GREEN on physical Xiaomi 22021211RI).

### FLOW-PROV-BOOKINGS-001 — View My Bookings → booking detail (mapped, read-only)
- steps: home tab1 → My Bookings (All, 2 Completed) → View Service Details → detail top + route plan → BACK
- screens: AND-PROV-BOOKINGS-001 → AND-PROV-REQDETAIL-001/002 | status: AUTOMATED + VERIFIED (`ProviderNavigationSmokeTest` GREEN; empty-state fallback armed; no mutations)
### FLOW-PROV-WALLET-001 — View wallet gate (mapped, presence-only)
- steps: home tab2 → My Wallet → live balances view-only (Stripe gate gone in backend 2026-09-22; async-body wait added) → stop | status: AUTOMATED + VERIFIED (`ProviderNavigationSmokeTest` GREEN; zero financial taps)
### FLOW-PROV-NAV-001 — Provider safe navigation smoke (VERIFIED 2026-09-22)
- role: PROVIDER | `ProviderNavigationFlow` + `ProviderNavigationSmokeTest` GREEN (single session 64.91s; full provider package 2/2 GREEN 102.9s)
- sequence: login → home → profile → BACK → bookings → detail (if cards) → BACK → home-tab → wallet → settings → help → home-tab → settings → logout → session-end
- repairs: `isAuthenticated` absence-poll (login-title linger race), greeting 15s poll (suite-sequential slow auth), wallet async-body wait (gate→live-balances backend variation)

### FLOW-PROV-SETTINGS-001 — Settings review → authorized logout (mapped + executed 1x)
- steps: home tab3 → settings top → scroll → Logout + Okay → auth area (login OR role-select) | status: AUTOMATED + VERIFIED (`ProviderNavigationSmokeTest` GREEN; teardown only)

---

## Admin Flows

*(To be documented)*

---

## Cross-Role E2E Flows

### FLOW-XROLE-ACCEPT-001 — CC-E2E-ACCEPT-001 Happy Path to Progression Boundary (VERIFIED 2026-09-23)
- Lifecycle: `CC-E2E-ACCEPT-001`
- Sequence: Customer creates booking `#CC-CW-20260923-000006` ($35.35) → Customer verifies Confirmed state in My Bookings → Provider receives on-demand dispatch pin on Home Map → Provider taps Accept (15-min countdown begins) → Customer sync verifies assigned provider → Provider taps Start → Transitions to 'Enroute to pick up customer vehicle' (Mapbox navigation) → Progresses to 'Arrived at service pickup location' → Reaches safe prerequisite boundary (OTP + photo proof).
- Idempotency & Mutation safety: Maintained runtime ledger `FUNCTIONAL_STATE_LEDGER.json`, single mutation lock, single device lock, exactly 1 tap per transition, no blind retries.
- status: AUTOMATED + VERIFIED on Xiaomi 22021211RI (Android 14). Extent & PDF reports generated.