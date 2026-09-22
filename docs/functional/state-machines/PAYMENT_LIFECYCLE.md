# Payment Lifecycle State Machine

> OBSERVED (read-only, no mutation): customer cards show charge totals; provider detail
> shows provider payout for the same job class (values differ → platform fee exists,
> magnitude UNKNOWN); provider wallet shows available/pending balances + payment history
> rows (values masked in docs); customer has Payment Methods (GATED entry).
> Stripe Connect onboarding completed in backend OUTSIDE automation (no action taken).

```
[UNKNOWN] price quote (wizard summary — discover: is payment captured, authorized, or deferred?)
  ↓ submit
[UNKNOWN] pre-service payment vs post-service payment (discover from summary + wallet deltas)
  ↓ service completes
[UNKNOWN] capture / payout split / fee / refund behavior (observe, never trigger)
```

Policy: read-only analysis first. Sandbox/test-rail automation ONLY if safe infra is
proven. Live charges, withdrawals, refunds, card/bank setup stay separately gated.
If booking SUBMIT itself requires a live charge → STOP before submit and report.
