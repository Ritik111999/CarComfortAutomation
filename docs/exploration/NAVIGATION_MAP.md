# Car Comfort Android Navigation Map

> Source: `ANDROID_SCREEN_GRAPH.json` (11 nodes / 12 edges). Rule: BACK only from pushed screens; tab returns via home-tab tap.

```
ROLE_SELECTION ──select Customer──▶ LOGIN ──provider creds──▶ PROV_HOME ─┬─tab1─▶ BOOKINGS ──View Details──▶ REQDETAIL ──scroll──▶ REQDETAIL_ROUTE
                                                                      │         └─BACK──┘ (pop, safe)
                                                                      ├─tab2─▶ WALLET (Stripe-gated, view only)
                                                                      ├─tab3─▶ SETTINGS_TOP ──scroll──▶ SETTINGS_LEGAL ──Logout+Okay──▶ LOGIN
                                                                      │            (Switch/Profile/Payment/Wallet/History/Tutorials/Learning/File/Report rows: presence-only)
                                                                      ├─tab4─▶ HELP (read-only)
                                                                      └─tab0─▶ HOME (self-loop)
LOGIN ──customer creds──▶ CUST_VEHICLE_GATE ⛔ ──BACK──▶ CUST_LICENSE_GATE ⛔
   (Save Vehicle / Cancel Signup / Upload license: ALL GATED — stop)
```

## Edge table (safe unless noted)

| # | From | Action (locator) | To | Safe | Note |
|---|------|------------------|----|------|------|
| 1 | PROV_HOME | tap tab1 (bottom_tab_index_1) | BOOKINGS | yes | read-only list, 2 Completed cards |
| 2 | BOOKINGS | tap View Service Details (acc-id) | REQDETAIL | yes | pushed; BACK pops |
| 3 | REQDETAIL | scroll forward (UiScrollable) | REQDETAIL_ROUTE | yes | route plan below fold |
| 4 | REQDETAIL | system BACK | BOOKINGS | yes | pushed only |
| 5 | PROV_HOME | tap tab2 (bottom_tab_index_2) | WALLET | yes-view | NO CTA taps (Stripe gated) |
| 6 | PROV_HOME | tap tab3 (bottom_tab_index_3) | SETTINGS_TOP | yes | rows presence-only |
| 7 | SETTINGS_TOP | scroll forward | SETTINGS_LEGAL | yes | reveals legal + Support + Delete Account (never tap) + Logout |
| 8 | SETTINGS_LEGAL | tap Logout + Okay (acc-id) | LOGIN | yes | authorized 1x role transition |
| 9 | PROV_HOME | tap tab4 (bottom_tab_index_4) | HELP | yes | informational |
| 10 | PROV_HOME | tap tab0 | PROV_HOME | yes | self-loop (dedup) |
| 11 | LOGIN | submit customer login (acc-id Login) | CUST_VEHICLE_GATE | NO (gate) | lands on bootstrap, not home |
| 12 | CUST_VEHICLE_GATE | system BACK | CUST_LICENSE_GATE | yes-dismiss | no save; Uploads GATED |

## Shortest safe paths
- Bookings detail: HOME → tab1 → View Details (3 hops)
- Wallet: HOME → tab2 (2 hops)
- Logout: HOME → tab3 → scroll → Logout+Okay (4 hops)
- Help: HOME → tab4 (2 hops)

## Unexplored safe edges (deferred, not blocked)
- Settings legal rows → detail pages (Privacy/Terms/Waiver/NDA/Contractor/Vehicle Agreement, Earnings PDF, Notifications, Tutorials, Learning Center, Profile Settings, Booking History, File a Claim, Report flows): presence-recorded, detail entry deferred to keep mapping goal (all read-only candidates, no gated risk except Payment/Wallet/Stripe).
- Bookings filters beyond `All` (none observed; conditional on pending/active jobs).
- Provider Profile via avatar from HOME (prior-modelled; re-verify via `ProviderAuthFlow.openProfile()` next provider session — no rediscovery).
- Bell/notifications on both roles (provider bell mapped as candidate; customer bell unreachable behind gate).
- Floating map buttons (location/SOS candidates — untapped, need classification).

## Conditional / unreachable (state-gated)
- All CUSTOMER home flows (browse/booking/my-bookings/history/support) until vehicle+license bootstrap authorized.
- Pending/active job actions (Accept/Reject/Complete/Cancel) — no live jobs in account; policy-GATED regardless.
