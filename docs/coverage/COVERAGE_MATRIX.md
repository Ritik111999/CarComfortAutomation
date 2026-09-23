# Car Comfort Automation Coverage Matrix

## Coverage States
- **UNKNOWN** - Not yet discovered
- **DISCOVERED** - Screen/flow identified, not yet modeled
- **MODELLED** - Page objects/components created, not automated
- **AUTOMATED** - Tests written, not yet verified on device
- **VERIFIED** - Tests executed successfully on physical device
- **BLOCKED** - Automation blocked by technical limitation
- **GATED** - Requires explicit authorization to execute

---

## Android Application - Customer Role

| ID | Feature | Screen/Flow | Status | Test ID | Last Verified | Notes |
|----|---------|-------------|--------|---------|---------------|-------|
| CUST-001 | App Launch | Splash → Login | VERIFIED | CustomerNavigationSmokeTest | 2026-09-22 | Full smoke green on device |
| CUST-002 | Login | Login Screen | VERIFIED | CustomerLoginSmokeTest | 2026-09-22 | Login + logout contract green |
| CUST-003 | Service Browse | Service List | VERIFIED | CustomerNavigationSmokeTest | 2026-09-22 | 3 wizard step-1 screens verified; submission GATED |
| CUST-004 | Booking Flow | Booking Wizard | VERIFIED | CustomerNavigationSmokeTest | 2026-09-22 | Location steps verified; steps 2-4 + submit GATED/deferred |
| CUST-005 | Booking Management | My Bookings | VERIFIED | CustomerNavigationSmokeTest | 2026-09-22 | List + Active empty state verified |
| CUST-006 | Profile | Profile Screen | VERIFIED | CustomerNavigationSmokeTest | 2026-09-22 | 13-row profile verified (PII masked) |
| CUST-007 | Notifications | Notification Center | MAPPED | - | 2026-09-22 | Bell edge deferred; settings row known |
| CUST-008 | Payment | Payment Methods | GATED | - | 2026-09-22 | Row presence-only; card/bank setup GATED |
| CUST-009 | History | Booking History | MAPPED | - | 2026-09-22 | Row + bookings list cover states; dedicated history entry deferred |
| CUST-010 | Support | Help/Support | VERIFIED | CustomerNavigationSmokeTest | 2026-09-22 | Support page + contact rows verified read-only |

---

## Android Application - Service Provider Role

| ID | Feature | Screen/Flow | Status | Test ID | Last Verified | Notes |
|----|---------|-------------|--------|---------|---------------|-------|
| PROV-001 | Provider Login | Login Screen | VERIFIED | ProviderLoginSmokeTest | 2026-09-22 | GREEN incl. profile/settings/logout path |
| PROV-002 | Dashboard | Provider Dashboard | VERIFIED | ProviderNavigationSmokeTest | 2026-09-22 | Greeting-verified (transition-tolerant poll) |
| PROV-003 | Booking Acceptance | Pending Bookings | VERIFIED | ProviderNavigationSmokeTest | 2026-09-22 | List + read-only detail verified; empty-state fallback armed; mutations GATED |
| PROV-004 | Service Management | Services CRUD | UNKNOWN | - | - | Not observed |
| PROV-005 | Availability | Schedule Management | UNKNOWN | - | - | Not observed |
| PROV-006 | Earnings | Earnings/Payouts | VERIFIED | ProviderNavigationSmokeTest | 2026-09-22 | Live balances view-only verified; Stripe gate gone (backend); no financial tap |
| PROV-007 | Profile | Provider Profile | VERIFIED | ProviderNavigationSmokeTest | 2026-09-22 | VERIFIED DRIVER + onboarding rows verified (PII masked) |
| PROV-008 | Verification | KYC/Verification | GATED | - | 2026-09-22 | No verification action taken; Stripe completed in backend outside automation |

---

## Android Application - Admin Role

| ID | Feature | Screen/Flow | Status | Test ID | Last Verified | Notes |
|----|---------|-------------|--------|---------|---------------|-------|
| ADMIN-001 | Admin Login | Login Screen | UNKNOWN | - | - | - |
| ADMIN-002 | User Management | Users List | UNKNOWN | - | - | - |
| ADMIN-003 | Provider Management | Providers List | UNKNOWN | - | - | - |
| ADMIN-004 | Booking Oversight | All Bookings | UNKNOWN | - | - | - |
| ADMIN-005 | Analytics | Dashboard Metrics | UNKNOWN | - | - | - |
| ADMIN-006 | System Config | Settings | UNKNOWN | - | - | - |

---

## Customer PWA

| ID | Feature | Screen/Flow | Status | Test ID | Last Verified | Notes |
|----|---------|-------------|--------|---------|---------------|-------|
| PWAC-001 | Landing Page | Home | UNKNOWN | - | - | - |
| PWAC-002 | Login/Register | Auth Flow | UNKNOWN | - | - | - |
| PWAC-003 | Service Search | Search/Filter | UNKNOWN | - | - | - |
| PWAC-004 | Booking | Booking Wizard | UNKNOWN | - | - | - |
| PWAC-005 | Account | My Account | UNKNOWN | - | - | - |

---

## Admin PWA

| ID | Feature | Screen/Flow | Status | Test ID | Last Verified | Notes |
|----|---------|-------------|--------|---------|---------------|-------|
| PWAA-001 | Admin Login | Auth | UNKNOWN | - | - | - |
| PWAA-002 | Dashboard | Overview | UNKNOWN | - | - | - |
| PWAA-003 | User Management | CRUD Users | UNKNOWN | - | - | - |
| PWAA-004 | Provider Management | CRUD Providers | UNKNOWN | - | - | - |
| PWAA-005 | Reports | Analytics | UNKNOWN | - | - | - |

---

## Cross-Role E2E Flows

| ID | Flow | Roles | Status | Test ID | Last Verified | Notes |
|----|------|-------|--------|---------|---------------|-------|
| E2E-001 | Customer Books → Provider Accepts → Progression | Customer, Provider | VERIFIED | BookingLifecycleE2ETest | 2026-09-23 | Live physical Android verified. Car Wash booking #CC-CW-20260923-000006 ($35.35) created, received on Provider Map dispatch pin, accepted, customer-synced, navigation started, arrived at pickup. Safely halted at OTP/Photo boundary. |
| E2E-002 | Customer Cancels → Provider Notified | Customer, Provider | UNKNOWN | - | - | Separate lifecycle |
| E2E-003 | Admin Views Booking → Verifies State | Admin | UNKNOWN | - | - | - |
| E2E-004 | Payment Flow → Provider Payout | Customer, Provider, Admin | UNKNOWN | - | - | - |

---

## Summary

| Platform | Role | Discovered | Automated | Verified | Blocked | Gated |
|----------|------|------------|-----------|----------|---------|-------|
| Android | Customer | 12 | 2 | 8 | 0 | 2 |
| Android | Provider | 8 | 2 | 6 | 0 | 1 |
| Android | Cross-Role | 4 | 1 | 1 | 0 | 0 |
| PWA | Customer | 0 | 0 | 0 | 0 | 0 |
| PWA | Admin | 0 | 0 | 0 | 0 | 0 |
| **Total** | | **24+1 shared** | **5** | **15** | **0** | **3** |

---

## Functional Coverage (L0–L5) — SCREEN COVERAGE != FUNCTIONAL COVERAGE

> Depth: L0 screen discovered · L1 navigation verified · L2 field/control behavior ·
> L3 single-role functional workflow · L4 cross-role E2E workflow · L5 negative/error/recovery.
> Canonical function inventory: `docs/functional/FUNCTIONALITY_REGISTRY.md`
> (+ `.json`). App v1.1.1 (io.carcomfort.app, versionCode 11). 2026-09-23.

| Function | Depth | Screen | Navigation | Function automated | Lifecycle E2E verified | Negative verified |
|----------|-------|--------|------------|--------------------|------------------------|-------------------|
| F-BOOK-WIZARD (wizard traversal) | L2 VERIFIED | VERIFIED | VERIFIED | VERIFIED | VERIFIED | L5 deferred |
| F-BOOK-CREATE (customer submit) | L3 VERIFIED | VERIFIED | VERIFIED | VERIFIED (CustomerBookingFlow) | VERIFIED (CC-E2E-ACCEPT-001 Phase 1, #CC-CW-20260923-000006, $35.35) | deferred |
| F-BOOK-RECEIVE (provider receipt) | L4 VERIFIED | VERIFIED | VERIFIED | VERIFIED (Provider on-demand Map pin) | VERIFIED (Phase 2, Map dispatch pin) | n/a (read-only) |
| F-BOOK-ACCEPT (provider accept) | L4 VERIFIED | VERIFIED | VERIFIED | VERIFIED (Provider on-demand sheet) | VERIFIED (Phases 3–4, Accept & customer sync) | deferred |
| F-BOOK-REJECT (separate lifecycle) | L4 UNKNOWN | VERIFIED | VERIFIED | UNKNOWN (NOT this milestone) | not started | deferred |
| F-SVC-PROGRESS (progression) | L4 VERIFIED | VERIFIED | VERIFIED | VERIFIED (ServiceExecutionFlow) | VERIFIED (Phase 5, Start -> Enroute -> Arrived at pickup) | deferred |
| F-SVC-COMPLETE (completion) | L4 GATED | VERIFIED | VERIFIED | AUTOMATED (boundary reached) | GATED (Requires live physical camera photo upload + Customer OTP PIN verification) | deferred |
| F-PAY-ANALYZE (payment arch) | L3 MODELLED | VERIFIED | VERIFIED | MODELLED (PaymentValidationFlow; mutations FORBIDDEN) | VERIFIED (Review total $35.35 captured; provider dispatch payout $85.00 observed) | deferred |
| F-CANCEL-* (3 scenarios) | L4 MODELLED | VERIFIED | VERIFIED | MODELLED (BookingCancellationFlow; NOT this milestone) | not started | deferred |
| F-BOOK-HISTORY (both roles) | L4 AUTOMATED | VERIFIED | VERIFIED | AUTOMATED | Historical observed; lifecycle booking in active progression | deferred |
| F-E2E-HAPPY-PATH (CC-E2E-ACCEPT-001) | L4 VERIFIED | VERIFIED | VERIFIED | VERIFIED (BookingLifecycleE2ETest) | VERIFIED (Phases 1–5 executed to PROGRESSION_BOUNDARY) | L5 deferred until E2E stable |

Booking-status evidence: Confirmed / Service Provider Not Assigned (OBSERVED fresh card) · Accepted (OBSERVED 15-min countdown) ·
Enroute to pick up customer vehicle (OBSERVED turn-by-turn navigation) · Arrived at service pickup location (OBSERVED arrival action).
Completed (OBSERVED historical both roles). See `docs/functional/BOOKING_STATUS_MATRIX.md`.

*Last Updated: 2026-09-23 (CC-E2E-ACCEPT-001 functional lifecycle executed live on Xiaomi 22021211RI).*