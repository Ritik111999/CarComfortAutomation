# Booking Status Matrix (evidence-backed)

> One row per OBSERVED state. HYPOTHESIZED rows are clearly marked and gain their
> action cells only after live discovery. PII/amounts masked.

| State | Customer sees | Provider sees | Available Customer actions | Available Provider actions | Next states |
| ----- | ------------- | ------------- | -------------------------- | -------------------------- | ----------- |
| Confirmed / Not Assigned (OBSERVED, fresh card) | Card: service, "Service Provider Not Assigned", "Confirmed", schedule, venue (masked), View Service Details | TBD (no fresh booking observed) | View details; (cancel TBD) | TBD | ACCEPTED? (discover) |
| Completed (OBSERVED) | Card: provider name/rating, "Completed", charge, schedule; detail TBD | "BOOKING STATUS Completed", payout, route plan, after-service photos/billing note | View details; (rate? TBD) | None (terminal) | HISTORY (TBD) |
| Cancelled by service provider (OBSERVED) | Card: "Cancelled by service provider", "Cancelled", amount, schedule | TBD | View details; (rebook? TBD) | None (terminal) | — |
| Submitted initial (HYPOTHESIZED label set) | — | — | — | Accept/Reject? (discover) | ACCEPTED / REJECTED (discover) |
| Accepted (HYPOTHESIZED) | TBD | TBD | TBD | progression verbs (discover) | IN_PROGRESS (discover) |
| In progress (HYPOTHESIZED) | TBD | TBD | TBD | Complete? (discover) | COMPLETED (discover) |

Populate TBD cells from the CC-E2E-ACCEPT-001 run only.
