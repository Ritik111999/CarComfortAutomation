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
### FLOW-CUST-SETTINGS-001 — Settings review → authorized logout (VERIFIED 2026-09-22)
- steps: home tab3 → settings → scroll → Log Out + Okay → login form | status: AUTOMATED + VERIFIED (`CustomerNavigationSmokeTest` GREEN; teardown only)

---

## Provider Flows

### FLOW-PROV-AUTH-001 — Provider login → home → profile → settings → logout (VERIFIED 2026-09-22)
- role: PROVIDER | status: AUTOMATED + VERIFIED (`ProviderAuthFlow`, `ProviderLoginSmokeTest` GREEN; logout accepts login-form OR role-select markers — back-stack-dependent, same lesson as customer)
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

### XROLE-BOOKING-001 (candidate, NOT executed)
- Customer booking `#CC-CB-20260915-000013` (Completed, EV+wash, $124.99) ↔ Provider My Bookings card + detail (same ID, payout $124.99) — IDs match across roles; lifecycle execution remains owner-authorized.