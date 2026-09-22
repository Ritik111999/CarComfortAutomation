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
| PROV-001 | Provider Login | Login Screen | AUTOMATED | ProviderLoginSmokeTest | 2026-09-22 | Session-restore reused; logout→login verified 1x |
| PROV-002 | Dashboard | Provider Dashboard | MODELLED | - | 2026-09-22 | Home mapped 4 routes + modelled |
| PROV-003 | Booking Acceptance | Pending Bookings | MAPPED | - | 2026-09-22 | List + Completed detail mapped; pending actions GATED, no live jobs |
| PROV-004 | Service Management | Services CRUD | UNKNOWN | - | - | Not observed |
| PROV-005 | Availability | Schedule Management | UNKNOWN | - | - | Not observed |
| PROV-006 | Earnings | Earnings/Payouts | MAPPED | - | 2026-09-22 | Wallet mapped; Stripe verification GATED |
| PROV-007 | Profile | Provider Profile | MODELLED | - | 2026-09-22 | Prior verified model (VERIFIED DRIVER); re-verify next session |
| PROV-008 | Verification | KYC/Verification | GATED | - | 2026-09-22 | Stripe + licenseVerification GATED |

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
| Android | Provider | 8 | 1 | 0 | 0 | 2 |
| Android | Admin | 0 | 0 | 0 | 0 | 0 |
| PWA | Customer | 0 | 0 | 0 | 0 | 0 |
| PWA | Admin | 0 | 0 | 0 | 0 | 0 |
| **Total** | | **20+1 shared** | **3** | **7** | **0** | **5** |

*Last Updated: 2026-09-22 (21 nodes / 22 edges in ANDROID_SCREEN_GRAPH.json; B-002 resolved; customer bell + wizard steps 2-4 + customer booking detail deferred; CustomerNavigationSmokeTest + CustomerLoginSmokeTest GREEN on device 2026-09-22 (2/2 in one session).*