# Provider Job Lifecycle State Machine

> Provider-side view of the same booking object. OBSERVED: My Bookings list
> (All filter, Completed cards with payout), booking detail (BOOKING ID, BOOKING
> STATUS, CURRENT STATUS, service/route/earnings), wallet (balances, history).

```
[UNKNOWN] incoming request (initial provider-side status wording — discover on fresh booking)
  ↓ accept (gated, CC-E2E-ACCEPT-001 only)
[UNKNOWN] accepted/active job states + progression verbs (discover live)
  ↓ progress → complete (gated)
[OBSERVED] Completed + payout + route plan (historical bookings)
```

Correlation rule: booking ID first, else scheduled datetime + service (+ customer alias
where safe). NEVER list position unless no alternative exists. NEVER touch another
customer's job — prove correlation before any mutation tap.
