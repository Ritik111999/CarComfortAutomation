# Workflow: Coverage audit

## audit-coverage

1. Cross-check COVERAGE_MATRIX against SCREEN_CATALOG + FLOW_CATALOG + test sources.
2. Counts must reconcile: every AUTOMATED flow has a test ID; every VERIFIED flow has execution evidence (report + commit SHA).
3. List UNKNOWN/DISCOVERED backlog (next actions), BLOCKED items (with blocker IDs), and GATED items (with case names).
4. Write findings to `docs/automation/STATE.md` (last audit + next recommended action).
