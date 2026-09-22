# Workflow: Safe Android exploration

## explore-next-customer-flow

1. Read `docs/automation/STATE.md`, `SCREEN_CATALOG.md`, `COVERAGE_MATRIX.md` for the next UNKNOWN flow.
2. Verify device: `scripts/device-status.sh`; acquire lock (owner = your agent id).
3. Explore in SAFE MODE (Appium MCP preferred): hierarchy + screenshots, read-only navigation first.
4. For each screen, append a SCREEN_CATALOG entry (actions, destinations, locators, side effects, gated actions).
5. Mark coverage DISCOVERED; file AUTOMATION_BLOCKERs for unexposable elements.
6. Release the device lock. Never perform gated actions.
