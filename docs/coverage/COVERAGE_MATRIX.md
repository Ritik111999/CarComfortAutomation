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
| CUST-001 | App Launch | Splash → Login | MAPPED | - | 2026-09-22 | Login mapped; home gated by B-002 |
| CUST-002 | Login | Login Screen | AUTOMATED | CustomerLoginSmokeTest | 2026-09-22 | Executes; lands on vehicle gate (prior verified) |
| CUST-003 | Service Browse | Service List | BLOCKED | - | 2026-09-22 | Behind vehicle/license gate (B-002) |
| CUST-004 | Booking Flow | Booking Wizard | BLOCKED | - | 2026-09-22 | Behind gate (B-002); commit actions GATED regardless |
| CUST-005 | Booking Management | My Bookings | BLOCKED | - | 2026-09-22 | Behind gate (B-002) |
| CUST-006 | Profile | Profile Screen | MODELLED | - | 2026-09-22 | Modelled prior run; unreachable this session (B-002) |
| CUST-007 | Notifications | Notification Center | BLOCKED | - | 2026-09-22 | Behind gate (B-002) |
| CUST-008 | Payment | Payment Methods | GATED | - | 2026-09-22 | Entry row known; card/bank setup GATED |
| CUST-009 | History | Booking History | BLOCKED | - | 2026-09-22 | Behind gate (B-002) |
| CUST-010 | Support | Help/Support | BLOCKED | - | 2026-09-22 | Behind gate (B-002) |

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
| Android | Customer | 3 | 1 | 0 | 7 | 2 |
| Android | Provider | 8 | 1 | 0 | 0 | 2 |
| Android | Admin | 0 | 0 | 0 | 0 | 0 |
| PWA | Customer | 0 | 0 | 0 | 0 | 0 |
| PWA | Admin | 0 | 0 | 0 | 0 | 0 |
| **Total** | | **11** | **2** | **0** | **7** | **4** |

*Last Updated: 2026-09-22 (mapping run; 11 unique nodes in ANDROID_SCREEN_GRAPH.json; customer home flows blocked by B-002 onboarding gate; no TestNG product assertions executed this run — verification = mapping evidence only).*