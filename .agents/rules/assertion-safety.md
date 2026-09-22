# Rule: Assertion safety

## assertion-safety

- Use `SafeAssertions` (soft assertions); call `finalizeAssertions()` to surface failures.
- NEVER change an expected business value to match the actual app output. Mismatches are `POTENTIAL_PRODUCT_DEFECT / REQUIREMENT_REVIEW_REQUIRED`.
- Classify every failure with `FailureClassifier`. Repair only `LOCATOR_DRIFT`, `SYNC_ISSUE`, `AUTOMATION_DEFECT`.
- Never suppress exceptions or remove assertions to get green results.
