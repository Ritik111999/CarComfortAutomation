# Workflow: Regression + repair (automation defects only)

## regression-repair-only

1. Run the target suite (e.g. `mvn test -DsuiteXmlFile=src/test/resources/suites/android-regression.xml`).
2. For each failure: collect evidence, classify with `FailureClassifier`.
3. Repair ONLY `LOCATOR_DRIFT`, `SYNC_ISSUE`, `AUTOMATION_DEFECT`. Application defects go to KNOWN_BLOCKERS + defect evidence, never weakened assertions.
4. Re-run the affected subset, then the suite; update coverage + state docs.
5. Gated groups are never included; device locks respected throughout.
