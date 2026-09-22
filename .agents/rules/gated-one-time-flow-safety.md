# Rule: Gated flow safety

## gated-one-time-flow-safety

- Categories `GATED, ONE_TIME, VERIFICATION, PAYMENT_SETUP, ACCOUNT_BOOTSTRAP, DESTRUCTIVE` never run in smoke/regression/CI/exploratory/repair runs.
- Execution requires BOTH `RUN_GATED_TESTS=true` AND `GATED_CASE=<case>` matching the test's categories; `GatedTestGuard` aborts otherwise.
- `gated-one-time.xml` entries stay `enabled="false"` by default; `scripts/run-gated.sh <CASE>` is the only entry point.
- Safe mode exploration must not create accounts, verify identity, touch Stripe/payments, or delete data.
