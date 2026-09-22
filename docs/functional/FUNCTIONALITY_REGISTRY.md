# Car Comfort Functionality Registry

> Canonical inventory of PRODUCT functionality (not screens). Screen coverage lives in
> `docs/exploration/SCREEN_CATALOG.md`; this file tracks what the product DOES.
> Depth: L0 discovered → L1 navigation → L2 field/control → L3 single-role workflow → L4 cross-role E2E → L5 negative.
> Smoke era proved L1; this registry drives L3/L4. Machine copy: `FUNCTIONALITY_REGISTRY.json`.

## Booking lifecycle

### F-BOOK-WIZARD — Customer booking wizard traversal (all 4 steps mapped 2026-09-22)
- platform: Android | role: CUSTOMER | module: booking
- sequence: 1 Location (manual address + suggestion pick; Next blocked until set; GPS disabled) → 2 Service (membership radio; custom Package Name*/Price* OR CarComfort Packages dropdown; ASAP default vs Schedule→time picker→date control UNKNOWN) → 3 Vehicle (default saved BMW; 4 required dropdowns: parking/provider-park/key/key-return option lists mapped) → 4 Review & Confirm (location/service/date/time/duration/vehicle/total sections, Edit links, Total $, Confirm Booking CTA; NO payment/card UI at submit)
- business rules: Next is validation-gated (silent no-op + disabled state when invalid); keyboard open can virtualize schedule section (hideKeyboard-only close; BACK pops wizard); step indicator 1-4
- expected transition: step N → step N+1 (navigation only, no business object yet)
- side effects: none (no submit) | screens: AND-CUST-SVC-* | opposite-role: none yet
- discoveryStatus: UNDERSTOOD | automationStatus: MODELLED | verificationStatus: VERIFIED (all steps traversed, submit never pressed)
- gated: false (until submit) | destructive: false | repeatable: true | requiresFreshData: false
- lastVerifiedAppVersion: 1.1.1 | notes: Schedule date-picker control undiscovered (time picker mapped); submit boundary = Confirm Booking

### F-BOOK-CREATE — Customer creates + submits a test booking
- platform: Android | role: CUSTOMER | module: booking
- description: Fill wizard end-to-end and submit ONE controlled test booking (far-future schedule, designated accounts).
- preconditions: F-BOOK-WIZARD steps mapped; BUSINESS_CASE authorized; ledger free of an open lifecycle for the ID
- input data: `BookingTestDataFactory.happyPathBooking()` (service, +21d date, time slot, manual test address, first saved vehicle)
- actions: wizard fill per discovered rules → summary review → SUBMIT (exactly once; idempotency: skip if ledger shows SUBMITTED)
- business rules: package name/price free entry (custom) or CarComfort dropdown; ASAP default (no date control needed); Schedule requires Date+Time (time picker mapped, date control TBD); vehicle defaults to first saved; parking/key dropdowns required
- expected transition: (no object) → SUBMITTED/WAITING_FOR_PROVIDER (exact label TBD from UI)
- payment at submit: NO payment/card UI on Review screen (observed) — submit creates obligation without immediate visible charge; post-service capture TBD (STOP if live charge encountered)
- side effects: creates a real booking visible to provider pool — ONLY the designated test booking
- related screens: AND-CUST-SVC-*, AND-CUST-BOOKINGS-001 | opposite-role: F-BOOK-RECEIVE
- discoveryStatus: UNKNOWN (submit behavior undiscovered) | automationStatus: UNKNOWN | verificationStatus: UNKNOWN
- gated: true (BUSINESS_CASE=BOOKING_LIFECYCLE_HAPPY_PATH) | destructive: false | repeatable: false (one booking per lifecycle ID) | requiresFreshData: true
- lastVerifiedAppVersion: - | notes: STOP before submit if a live financial charge is required

### F-BOOK-RECEIVE — Provider sees the incoming test booking
- platform: Android | role: PROVIDER | module: bookings
- description: New customer booking propagates to provider My Bookings; correlate WITHOUT list position (booking ID / scheduled datetime / service).
- preconditions: F-BOOK-CREATE SUBMITTED | actions: open bookings, locate by discriminator, open detail read-only
- expected transition: (provider unaware) → request visible with initial status
- discoveryStatus: UNDERSTOOD (pattern known from historical bookings) | automationStatus: UNKNOWN | verificationStatus: UNKNOWN
- gated: false (read-only) | repeatable: true | requiresFreshData: true (needs the fresh booking)

### F-BOOK-ACCEPT — Provider accepts the test booking
- platform: Android | role: PROVIDER | module: bookings
- description: Accept action + confirmation dialog + pre/post states on both roles. Exactly one accept on the designated booking (idempotency: skip if already accepted).
- preconditions: booking in SUBMITTED/WAITING state; mutation lock held | actions: Accept → confirm → verify provider state → verify customer state
- expected transition: SUBMITTED → ACCEPTED (exact labels TBD)
- side effects: booking leaves provider pool; customer sees new status
- gated: true (same BUSINESS_CASE; designated booking only — never another customer's job) | destructive: false | repeatable: false
- discoveryStatus: UNKNOWN (Accept UI undiscovered) | automationStatus: UNKNOWN | verificationStatus: UNKNOWN

### F-BOOK-REJECT — Provider rejects a test booking (separate lifecycle CC-E2E-REJECT-001)
- description: Reject + reason + confirm; provider/customer/history states. Requires its OWN fresh booking; never the accept-booking.
- discoveryStatus: UNKNOWN | automationStatus: UNKNOWN | gated: true | notes: NOT this milestone (happy path first)

### F-SVC-PROGRESS — Service execution progression (Start/OnTheWay/Arrived/...)
- description: Real progression actions as rendered by the app (labels TBD from live detail — never invented).
- preconditions: ACCEPTED booking; mutation lock | actions: one transition at a time, verify both roles, ledger each step
- discoveryStatus: UNKNOWN | automationStatus: UNKNOWN | gated: true

### F-SVC-COMPLETE — Service completion
- description: Completion prerequisites (photos/notes?), confirm, customer confirmation, payment consequences, rating prompt, history.
- preconditions: final progression state; mutation lock | gated: true
- discoveryStatus: UNKNOWN | automationStatus: UNKNOWN | notes: STOP if completion triggers a live charge not covered by authorization

### F-PAY-ANALYZE — Payment architecture discovery (read-only first)
- description: Classify captured/authorized/cash/card/wallet/Stripe/pre/post-service, earnings/fee/refund from UI + wallet deltas. Sandbox automation only if safe infra exists; live mutation stays gated.
- discoveryStatus: UNKNOWN | automationStatus: UNKNOWN | gated: true (mutation side)

### F-CANCEL-CUST-BEFORE / F-CANCEL-CUST-AFTER / F-CANCEL-PROV — Cancellation scenarios
- description: Separate bookings, separate lifecycle IDs, separate expected outcomes (late-fee rules TBD).
- discoveryStatus: UNKNOWN | automationStatus: UNKNOWN | gated: true | notes: NOT this milestone

### F-BOOK-HISTORY — Booking appears in history (both roles)
- preconditions: COMPLETED lifecycle | actions: open history/bookings, locate by booking ID | discoveryStatus: UNDERSTOOD | automationStatus: UNKNOWN

## Cross-role E2E

### F-E2E-HAPPY-PATH — CC-E2E-ACCEPT-001: create → receive → accept → customer-accept-verify → progress → complete → customer-complete-verify → history
- discoveryStatus: UNKNOWN | automationStatus: UNKNOWN | verificationStatus: UNKNOWN
- gated: true (BUSINESS_CASE=BOOKING_LIFECYCLE_HAPPY_PATH) | repeatable: false | requiresFreshData: true
