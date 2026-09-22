# Architecture Overview

## Layers
Tests (business intent) → Business Flows → Component Objects → Screen/Page Objects → Core
(driver / device / config / waits / assertions / logging / reporting / evidence / test-data / guards / diagnostics).

## Key decisions
- Real physical devices only; file-mutex device locks with heartbeat + stale cleanup.
- Android: Appium UiAutomator2 black-box over the Flutter accessibility hierarchy (no Flutter Driver dependency).
- iOS prepared via `IosDriverManager` (XCUITest/WDA) but inactive until a physical iPhone is provisioned.
- PWAs via Selenium `WebDriverManager(customer|admin)`, sharing core infra with mobile.
- Reporting behind `ReportEngine` (Extent HTML) + `PdfReportGenerator` (PDFBox) + JSON summary.
- Gated flows dual-authorized (`RUN_GATED_TESTS=true` + `GATED_CASE`), excluded from all normal suites.
- Failures classified (`FailureClassifier`); only locator/sync/automation defects are auto-repaired.

See `docs/decisions/ARCHITECTURE_DECISIONS.md` for the ADR log.
