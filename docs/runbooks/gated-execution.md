# Runbook: Gated execution

1. Owner provides explicit authorization: case name (e.g. `CARD_SETUP`).
2. Export `RUN_GATED_TESTS=true` and `GATED_CASE=<case>`.
3. Execute ONLY `scripts/run-gated.sh <CASE>` (suite: `gated-one-time.xml`).
4. Without both variables the guard aborts before any device action — this is expected, not a failure.
5. Record side effects + evidence in STATE.md and GATED_FLOWS.md.
