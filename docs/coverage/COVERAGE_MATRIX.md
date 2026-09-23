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
| E2E-001 | Customer Books → Provider Accepts → Complete | Customer, Provider | UNKNOWN | - | - | - |
| E2E-002 | Customer Cancels → Provider Notified | Customer, Provider | UNKNOWN | - | - | - |
| E2E-003 | Admin Views Booking → Verifies State | Admin | UNKNOWN | - | - | - |
| E2E-004 | Payment Flow → Provider Payout | Customer, Provider, Admin | UNKNOWN | - | - | - |

---

## Summary

| Platform | Role | Discovered | Automated | Verified | Blocked | Gated |
|----------|------|------------|-----------|----------|---------|-------|
| Android | Customer | 12 | 2 | 7 | 0 | 3 |
| Android | Provider | 8 | 2 | 5 | 0 | 2 |
| Android | Admin | 0 | 0 | 0 | 0 | 0 |
| PWA | Customer | 0 | 0 | 0 | 0 | 0 |
| PWA | Admin | 0 | 0 | 0 | 0 | 0 |
| **Total** | | **20+1 shared** | **4** | **12** | **0** | **5** |

---

## Functional Coverage (L0–L5) — SCREEN COVERAGE != FUNCTIONAL COVERAGE

> Depth: L0 screen discovered · L1 navigation verified · L2 field/control behavior ·
> L3 single-role functional workflow · L4 cross-role E2E workflow · L5 negative/error/recovery.
> Smoke era proved mostly L1. Canonical function inventory: `docs/functional/FUNCTIONALITY_REGISTRY.md`
> (+ `.json`). App v1.1.1 (io.carcomfort.app, versionCode 11). Commit 57cc051. 2026-09-23.

| Function | Depth | Screen | Navigation | Function automated | Lifecycle E2E verified | Negative verified |
|----------|-------|--------|------------|--------------------|------------------------|-------------------|
| F-BOOK-WIZARD (wizard traversal) | L2 VERIFIED | VERIFIED | VERIFIED | MODELLED | n/a (no object) | L5 deferred (field rules documented, negative after happy path) |
| F-BOOK-CREATE (customer submit) | L3 AUTOMATED | VERIFIED | VERIFIED | AUTOMATED (CustomerBookingFlow; awaiting authorized run) | pending (CC-E2E-ACCEPT-001 Phase 1) | deferred |
| F-BOOK-RECEIVE (provider receipt) | L4 AUTOMATED | VERIFIED | VERIFIED | AUTOMATED (ProviderBookingFlow.findLifecycleBooking) | pending (Phase 2) | n/a (read-only) |
| F-BOOK-ACCEPT (provider accept) | L4 AUTOMATED | VERIFIED | VERIFIED | AUTOMATED (ProviderBookingFlow.acceptCurrentBooking) | pending (Phases 3–4) | deferred |
| F-BOOK-REJECT (separate lifecycle) | L4 UNKNOWN | VERIFIED | VERIFIED | UNKNOWN (NOT this milestone) | not started | deferred |
| F-SVC-PROGRESS (progression) | L4 AUTOMATED | VERIFIED | VERIFIED | AUTOMATED (ServiceExecutionFlow.progressOnce) | pending (Phase 5) | deferred |
| F-SVC-COMPLETE (completion) | L4 AUTOMATED | VERIFIED | VERIFIED | AUTOMATED (ServiceExecutionFlow.completeService) | pending (Phases 5–6) | deferred |
| F-PAY-ANALYZE (payment arch) | L3 MODELLED (read-only) | VERIFIED | VERIFIED | MODELLED (PaymentValidationFlow; mutations FORBIDDEN) | pending (totals across roles) | deferred |
| F-CANCEL-* (3 scenarios) | L4 MODELLED (scaffold) | VERIFIED | VERIFIED | MODELLED (BookingCancellationFlow; NOT this milestone) | not started | deferred |
| F-BOOK-HISTORY (both roles) | L4 AUTOMATED | VERIFIED | VERIFIED | AUTOMATED (E2E Phase 6 + DONE) | pending | deferred |
| F-E2E-HAPPY-PATH (CC-E2E-ACCEPT-001) | L4 AUTOMATED | VERIFIED | VERIFIED | AUTOMATED (BookingLifecycleE2ETest, 6 phases) | UNKNOWN (ledger at STARTED; awaiting RUN_BUSINESS_LIFECYCLE_TESTS=true + BUSINESS_CASE=BOOKING_LIFECYCLE_HAPPY_PATH) | L5 deferred until E2E stable |

Booking-status evidence: Confirmed / Not Assigned (OBSERVED fresh card) · Completed (OBSERVED both roles) ·
Cancelled by service provider (OBSERVED customer label) · Submitted-initial / Accepted / In-progress labels HYPOTHESIZED
until CC-E2E-ACCEPT-001 renders them (see `docs/functional/BOOKING_STATUS_MATRIX.md`).

*Last Updated: 2026-09-23 (functional depth split added; no screen re-analysis; app v1.1.1 unchanged).*