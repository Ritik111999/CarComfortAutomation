# Workflow: Automate a discovered flow

## automate-next-flow

1. Pick a DISCOVERED flow from COVERAGE_MATRIX; read its SCREEN_CATALOG entries.
2. Create/extend Screen + Component objects (`LocatorFactory` priority), then the Business Flow, then the TestNG test with groups (never gated groups in normal suites).
3. Run the single test against the locked physical device; fix LOCATOR_DRIFT/SYNC_ISSUE/AUTOMATION_DEFECT only.
4. Verify Extent + PDF + evidence outputs; mark AUTOMATED → VERIFIED in COVERAGE_MATRIX.
5. Update FLOW_CATALOG.md, SCREEN_CATALOG.md (automation status), KNOWN_BLOCKERS.md if needed.
