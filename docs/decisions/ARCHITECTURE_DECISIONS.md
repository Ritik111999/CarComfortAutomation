# Car Comfort Architecture Decisions

## Decision Log

### ADR-001: Physical Device Only Architecture
- **Date**: 2026-09-21
- **Status**: Accepted
- **Context**: Car Comfort automation must run on real physical devices only
- **Decision**: No emulator/simulator support in framework. All device management assumes physical hardware.
- **Consequences**: Requires device lab or physical devices for CI/CD. Device locking mandatory.

### ADR-002: Appium UiAutomator2 for Android Flutter
- **Date**: 2026-09-21
- **Status**: Accepted
- **Context**: Car Comfort Android app is Flutter-based
- **Decision**: Use Appium UiAutomator2 driver with accessibility/native hierarchy. Not Flutter driver.
- **Consequences**: Locator strategy prioritizes accessibility-id, resource-id, semantic text. No coordinate-based taps.

### ADR-003: Device Locking via File Mutex
- **Date**: 2026-09-21
- **Status**: Accepted
- **Context**: Multiple AI agents may run concurrently
- **Decision**: File-based locking in `artifacts/device-locks/` with heartbeat and stale cleanup
- **Consequences**: Prevents concurrent device access. Lock files survive process crashes.

### ADR-004: Gated Test Protection
- **Date**: 2026-09-21
- **Status**: Accepted
- **Context**: Some flows are irreversible (payments, verification, account creation)
- **Decision**: Dual-gate system requiring `RUN_GATED_TESTS=true` AND `GATED_CASE=<case>`
- **Consequences**: Normal CI/smoke/regression runs never execute gated tests. Explicit owner authorization required.

### ADR-005: Reporting Abstraction
- **Date**: 2026-09-21
- **Status**: Accepted
- **Context**: Need ExtentReports for HTML + PDF for QA + JSON for CI
- **Decision**: `ReportEngine` abstraction over ExtentReports. `PdfReportGenerator` for PDF output.
- **Consequences**: Tests never couple to Extent directly. Easy to swap reporters.

### ADR-006: Layered Architecture (Tests → Flows → Components → Screens → Core)
- **Date**: 2026-09-21
- **Status**: Accepted
- **Context**: Maintainability and reuse across roles/platforms
- **Decision**: Strict layering with business flows as primary reusable units
- **Consequences**: Tests describe intent only. No business logic in page objects.

### ADR-007: No Thread.sleep() - Explicit Waits Only
- **Date**: 2026-09-21
- **Status**: Accepted
- **Context**: Flaky tests from fixed sleeps
- **Decision**: `WaitStrategies` and `AndroidWaitStrategies` with FluentWait. `Thread.sleep()` throws exception.
- **Consequences**: More robust synchronization. Requires proper wait conditions.

### ADR-008: Safe Assertions with Soft Assert
- **Date**: 2026-09-21
- **Status**: Accepted
- **Context**: AI must never weaken expected values to match actual
- **Decision**: `SafeAssertions` with soft assertions. Evidence capture on failure. Classification of failures.
- **Consequences**: All assertion failures captured. Business logic assertions protected.

### ADR-009: Secret Handling
- **Date**: 2026-09-21
- **Status**: Accepted
- **Context**: Test data contains passwords, OTPs, tokens, PII
- **Decision**: `TestLogger.mask()` and `TestDataManager.maskForLogging()`. Patterns in config.
- **Consequences**: No secrets in logs, reports, or evidence. Config-driven masking.

### ADR-010: iOS Architecture Isolation
- **Date**: 2026-09-21
- **Status**: Accepted
- **Context**: iOS automation planned for future
- **Decision**: Separate `mobile/ios/` package. Same flow/business layer. XCUITest + WebDriverAgent.
- **Consequences**: No iOS assumptions in Android code. Shared business logic.

### ADR-011: Configuration via HOCON + Environment
- **Date**: 2026-09-21
- **Status**: Accepted
- **Context**: Multiple environments, device configs, secrets
- **Decision**: Typesafe Config (reference.conf) + system properties + env vars. No hardcoded values.
- **Consequences**: Single config source. Environment-specific overrides.

### ADR-012: Maven + TestNG + Java 21
- **Date**: 2026-09-21
- **Status**: Accepted
- **Context**: Build tool, test framework, language
- **Decision**: Maven for build, TestNG for testing, Java 21 LTS
- **Consequences**: Standard enterprise stack. Good IDE support. Parallel execution ready.