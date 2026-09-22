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

### FLOW-CUST-AUTH-001 — Customer login → vehicle gate (documented, not passable without bootstrap)
- role: CUSTOMER | screens: AND-SHARED-ROLE-001 → AND-SHARED-LOGIN-001 → AND-CUST-VEHICLE-001
- steps: select Customer → enter env creds (focus-tap) → submit → lands on Add Vehicle Information (Save/Cancel GATED, stop)
- automation status: AUTOMATED up to gate (`CustomerAuthFlow.loginAsCustomer` + `CustomerLoginSmokeTest`); home verification BLOCKED (B-002)

---

## Provider Flows

### FLOW-PROV-AUTH-001 — Provider login → home → profile → settings → logout
- role: PROVIDER | status: AUTOMATED (`ProviderAuthFlow`, `ProviderLoginSmokeTest`, verified prior run; session-restore reused this run)
### FLOW-PROV-BOOKINGS-001 — View My Bookings → booking detail (mapped, read-only)
- steps: home tab1 → My Bookings (All, 2 Completed) → View Service Details → detail top + route plan → BACK
- screens: AND-PROV-BOOKINGS-001 → AND-PROV-REQDETAIL-001/002 | status: MAPPED + MODELLED (no test yet; no mutations)
### FLOW-PROV-WALLET-001 — View wallet gate (mapped, presence-only)
- steps: home tab2 → My Wallet → Stripe gate shown → stop | status: MAPPED (GATED_CASE required to proceed)
### FLOW-PROV-SETTINGS-001 — Settings review → authorized logout (mapped + executed 1x)
- steps: home tab3 → settings top → scroll → Logout + Okay → login | status: MAPPED + EXECUTED (role transition only)

---

## Admin Flows

*(To be documented)*

---

## Cross-Role E2E Flows

### XROLE-BOOKING-001 (candidate, NOT executed)
- Customer booking `#CC-CB-20260915-000013` (Completed, EV+wash, $124.99) ↔ Provider My Bookings card + detail (same ID, payout $124.99) — IDs match across roles; lifecycle execution remains owner-authorized.