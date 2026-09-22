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
LOGIN ──customer creds──▶ CUST_HOME ─┬─tap Car Wash──▶ CARWASH_WIZARD( Location step; STOP before Next )──BACK──┘
                                    ├─tap EV Charging──▶ EV_WIZARD (Charging-type YES/NO present, untouched; STOP before Next)──BACK──┘
                                    ├─tap Combo──▶ COMBO_WIZARD (route-order note; STOP before Next)──BACK──┘
                                    ├─tab1─▶ CUST_BOOKINGS (Confirmed Not-Assigned + 2 Completed + scrolled Cancelled; detail entry deferred)
                                    ├─tab2─▶ CUST_ACTIVE (empty state: No Active Booking)
                                    ├─tab3─▶ CUST_SETTINGS ──scroll──▶ CUST_SETTINGS_LEGAL ──Log Out+Okay──▶ LOGIN (observed: login form)
                                    ├─tab4─▶ CUST_SUPPORT (Email/Chat/Call rows presence-only)
                                    ├─avatar─▶ CUST_PROFILE (13 rows; PII masked)──BACK──┘
                                    └─tab0─▶ HOME (self-loop)
LOGIN ──customer creds (one prior session only)──▶ CUST_VEHICLE_GATE ⛔ ──BACK──▶ CUST_LICENSE_GATE ⛔ (transient; cleared; Cancel taps=0; retained as historical gate record)
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
| 11 | LOGIN | submit customer login (acc-id Login) | CUST_HOME | yes | authorized; gate absent this run |
| 12 | CUST_HOME | tap Car Wash / EV Charging / Combo | *_WIZARD | yes | Location step inspect; STOP before Next (GATED_BUSINESS_ACTION boundary) |
| 13 | *_WIZARD | system BACK | CUST_HOME | yes | pop pushed wizard |
| 14 | CUST_HOME | tap tabs 1/2/3/4, avatar | BOOKINGS/ACTIVE/SETTINGS/SUPPORT/PROFILE | yes | rows presence-only; Payment/Switch/Delete never tapped |
| 15 | CUST_SETTINGS | scroll forward | CUST_SETTINGS_LEGAL | yes | reveals legal + Delete Account (never) + Log Out |
| 16 | CUST_SETTINGS_LEGAL | tap Log Out + Okay | LOGIN | yes | authorized teardown; lands on LOGIN form (flow-code note stale) |
| 17 | CUST_PROFILE | system BACK | CUST_HOME | yes | pop pushed profile |
| 18 | (historical) LOGIN | submit (one prior session) | CUST_VEHICLE_GATE | NO (gate) | transient onboarding; cleared; Cancel taps=0 |

## Shortest safe paths
- Provider detail: HOME → tab1 → View Details (3 hops); wallet/help: HOME → tab (2 hops); logout: HOME → tab3 → scroll → Logout+Okay (4 hops)
- Customer booking wizard: HOME → card → inspect step 1 → BACK (3 hops, never Next)
- Customer bookings: HOME → tab1 (2 hops)
- Customer logout: HOME → tab3 → scroll → Log Out+Okay (4 hops)

## Unexplored safe edges (deferred, not blocked)
- Customer bell (top-right notifications): tap deferred (avatar/profile path proven; bell next).
- Customer booking detail (View Service Details from customer list): deferred (provider detail maps the pattern).
- Customer wizard steps 2-4 (Service/Vehicle/Review) + Next: deferred — step 2+ advance toward GATED submission; needs submission-boundary scoping.
- Customer Payment Methods / Billing History / Switch to Service Provider entries: GATED/out-of-scope, presence-only.
- Settings legal rows → detail pages (Privacy/Terms/Waiver/NDA/Contractor/Vehicle Agreement, Earnings PDF, Notifications, Tutorials, Learning Center, Profile Settings, Booking History, File a Claim, Report flows): presence-recorded, detail entry deferred to keep mapping goal (all read-only candidates, no gated risk except Payment/Wallet/Stripe).
- Bookings filters beyond `All` (none observed; conditional on pending/active jobs).
- Provider Profile via avatar from HOME (prior-modelled; re-verify via `ProviderAuthFlow.openProfile()` next provider session — no rediscovery).
- Bell/notifications on both roles (provider bell mapped as candidate; customer bell unreachable behind gate).
- Floating map buttons (location/SOS candidates — untapped, need classification).

## Conditional / unreachable (state-gated)
- All CUSTOMER home flows (browse/booking/my-bookings/history/support) until vehicle+license bootstrap authorized.
- Pending/active job actions (Accept/Reject/Complete/Cancel) — no live jobs in account; policy-GATED regardless.
