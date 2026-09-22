# Booking Lifecycle State Machine

> States below marked OBSERVED come from live evidence (customer bookings list, provider
> bookings list + detail, all 2026-09-22, app v1.1.1). States marked HYPOTHESIZED are
> placeholders to confirm/refute during discovery — never asserted until observed.

```
[HYPOTHESIZED] DRAFT (wizard in progress, no object)
  ↓ submit (F-BOOK-CREATE, gated)
[OBSERVED rentals] SUBMITTED-INITIAL — customer wording for the newest card:
  "Confirmed" + "Service Provider Not Assigned" (customer list, Sep 17 booking)
  provider-side initial wording: TBD (no fresh booking observed yet)
  ↓ provider accept (F-BOOK-ACCEPT, gated)
[HYPOTHESIZED] ACCEPTED / ASSIGNED (confirm against live UI)
  ↓ service progression (F-SVC-PROGRESS, gated)
[HYPOTHESIZED] IN_PROGRESS substates (confirm: ON_THE_WAY / ARRIVED / STARTED / ...)
  ↓ complete (F-SVC-COMPLETE, gated)
[OBSERVED] COMPLETED — customer: "Completed" + provider name/rating; provider:
  "BOOKING STATUS Completed" + payout + after-service photos/billing note
[HYPOTHESIZED] HISTORY visibility (F-BOOK-HISTORY)

Terminal branches (observed labels):
[OBSERVED] CANCELLED — customer: "Cancelled by service provider" (Cancelled, $ amount)
[UNKNOWN] REJECTED — label TBD (separate lifecycle CC-E2E-REJECT-001, not this milestone)
```

## Observed transitions (evidence-backed)

| # | From | Action (role) | To | Evidence |
|---|------|---------------|----|----------|
| 1 | SUBMITTED-INITIAL? | none (new card) | Confirmed / Not Assigned (customer) | AND-CUST-NTAB1-001 |
| 2 | (service done) | (outside automation) | Completed (both roles, payout/charge differ) | AND-PROV-REQDETAIL-001, AND-CUST-NTAB1-001 |
| 3 | (provider cancelled) | (outside automation) | Cancelled by service provider (customer) | AND-CUST-NTAB1-001-SCROLL |

## Pending discovery (happy path)

T1 create/submit (incl. payment-at-submit? — STOP if live charge) · T2 provider receipt/initial status wording ·
T3 accept + dialog + both-side post-states · T4 customer accept-verify · T5 progression verbs (tap ONE at a time) ·
T6 complete (prereqs, proof, payment consequence) · T7 customer complete-verify · T8 history both roles.
