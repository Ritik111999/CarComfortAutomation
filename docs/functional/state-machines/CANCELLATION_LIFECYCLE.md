# Cancellation Lifecycle State Machine

> OBSERVED: exactly one terminal label — customer "Cancelled by service provider".
> Everything else below is HYPOTHESIZED scaffolding for later scoped tasks.
> NOT this milestone: each scenario needs its own booking + lifecycle ID.

```
SUBMITTED ──customer cancel (before accept)──▶ CANCELLED_BY_CUSTOMER [HYPOTHESIZED]
SUBMITTED ──provider reject w/ reason?───────▶ REJECTED [HYPOTHESIZED]
ACCEPTED ───customer cancel (after accept)───▶ CANCELLED_LATE (+fee?) [HYPOTHESIZED]
ACCEPTED ───provider cancel─────────────────▶ CANCELLED_BY_PROVIDER [OBSERVED label]
ANY ───────system cancel────────────────────▶ CANCELLED_SYSTEM [HYPOTHESIZED]
```

Rules/fees/refund consequences: UNKNOWN — discover per scenario, never reuse bookings across mutually exclusive branches.
