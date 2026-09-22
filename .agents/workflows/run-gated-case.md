# Workflow: Gated execution (explicit authorization only)

## run-gated-case

1. Confirm owner authorization: `RUN_GATED_TESTS=true` AND `GATED_CASE=<case>` (e.g. `CARD_SETUP`).
2. Run ONLY via `scripts/run-gated.sh <CASE>` / `mvn test -DsuiteXmlFile=.../gated-one-time.xml -DRUN_GATED_TESTS=true -DGATED_CASE=<CASE>`.
3. Without both flags, execution must abort before any device action — verify the guard message in logs.
4. Record outcome, evidence, and side effects in STATE.md + GATED_FLOWS.md. Never fold gated steps into normal suites.
