# Rule: Layered architecture

## layered-architecture

- Layers: Tests (intent) → Business Flows → Component Objects → Screen/Page Objects → Core (driver/device/config/wait/assert/log/report/evidence).
- Locators live in Screens/Components only. Business logic lives in Flows only. Driver init lives in managers only.
- Tests use `ReportEngine` (never Extent directly) and update `docs/coverage/COVERAGE_MATRIX.md` after every automation addition.
- Keep it maintainable: no duplicate wrappers, no giant page classes, no premature parallelism on mobile.
