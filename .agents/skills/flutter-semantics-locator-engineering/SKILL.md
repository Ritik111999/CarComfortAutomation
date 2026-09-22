---
name: flutter-semantics-locator-engineering
description: Use when creating a stable locator strategy for a Flutter screen, repairing a broken mobile locator, or judging whether an element is automatable at all.
---

# flutter-semantics-locator-engineering

## When to use
New screen modeling, NoSuchElement triage, choosing between accessibility-id / resource-id / text / UiAutomator / XPath.

## How Flutter exposes UI
The live release app is a black box: Flutter renders its Semantics tree through Android accessibility services. Inspect `content-desc, text, resource-id, class, clickable, enabled, selected, checked, bounds` plus the accessibility hierarchy (UiAutomator2, never Flutter Driver).

## Priority (strict)
1. accessibility-id / content-desc — `LocatorFactory.accessibilityId`
2. resource-id — `LocatorFactory.resourceId`
3. stable semantic text — `LocatorFactory.stableText`
4. UiAutomator selectors — `LocatorFactory.uiAutomator*` (prefer UiObject2-compatible API: `UiSelector` with stable attributes; legacy `UiSelector` text/class chains are unreliable across multiple windows — see system-dialogs skill)
5. constrained XPath — `LocatorFactory.constrainedXPath` (class + stable attribute required; bare `//*` and positional chains like `//android.view.View[4]/.../View[2]` are rejected unless a blocker entry documents why no stable locator exists)
6. Coordinates: FORBIDDEN as primary strategy.

## Rules
- Locators live in Screen/Component classes only, never in tests, never duplicated.
- Custom Flutter controls often expose weak semantics: attempt all five levels before declaring a blocker.
- On failure: classify LOCATOR_DRIFT, capture hierarchy + attempted locators, fix the locator — never weaken assertions.
- Impossible element → AUTOMATION_BLOCKER with screen, control, hierarchy excerpt, failed attempts, fallback if any, and recommended Flutter Semantics/accessibility fix.

## Authoritative References

- Flutter accessibility & Semantics — https://docs.flutter.dev/ui/accessibility
- Appium UiAutomator2 driver: element location — https://github.com/appium/appium-uiautomator2-driver

Last reviewed: 2026-09-22
