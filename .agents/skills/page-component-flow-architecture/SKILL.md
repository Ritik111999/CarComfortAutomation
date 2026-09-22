---
name: page-component-flow-architecture
description: Use when creating or reviewing any Screen/Page Object, reusable Component Object, or Business Flow, or when business logic leaks into the wrong layer.
---

# page-component-flow-architecture

## When to use
New screen modeling, fragment reuse, workflow composition, layering reviews (also the checklist for automation code review).

## Architecture (Selenium Page Object principles apply to mobile Screen Objects)
Screen/Page Object + Component Object + Business Flow + Test.

## Rules
- Screens (`BaseAndroidScreen` / PWA base pages): one class per screen, private `By` via `LocatorFactory`, public action/verification methods. A screen may assert it loaded correctly; it must not hold business pass/fail assertions. No driver init, no sleeps, no raw driver use from tests.
- Components (`BaseAndroidComponent`): repeated fragments (header, card, list item, dialog) scoped to a root locator; shared, never duplicated across screens.
- Flows (`BaseBusinessFlow`): business-intent methods composing screens/components; gated steps call `verifyGated` first; checkpoints logged; `finalizeAssertions()` at the end. No TestNG annotations in flows.
- Tests own business assertions via `SafeAssertions`; report through `ReportEngine` only.
- Review bar: no sleeps/coordinates/hardcoded UDIDs/secrets, no Extent imports in tests, docs updated (SCREEN_CATALOG, FLOW_CATALOG, COVERAGE_MATRIX, KNOWN_BLOCKERS).

## Authoritative References

- Selenium Page Objects — https://www.selenium.dev/documentation/test_practices/encouraged/page_object_models/
- Flutter testing conventions — https://docs.flutter.dev/testing

Last reviewed: 2026-09-22
