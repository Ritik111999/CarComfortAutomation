---
name: mobile-app-state-management
description: Use when launching, backgrounding, restarting, or checking install state of the Car Comfort app on the physical device.
---

# mobile-app-state-management

## When to use
App foreground/background, session-start launch, crash recovery, install checks.

## Rules
- Use `AppStateManager` (launch / terminate / restart / background / isInstalled / guardedReset).
- Never uninstall, clear data, or reset authenticated state unless the scenario explicitly authorizes it (`guardedReset(true)` only).
- Diagnose first on anomaly: crashed app vs session loss vs device offline (`physical-device-resilience`), and never convert a product failure into a pass by restarting blindly.

## Authoritative References

- Appium UiAutomator2 driver: app management — https://github.com/appium/appium-uiautomator2-driver
- Android Developers: activities — https://developer.android.com/guide/components/activities/intro-activities

Last reviewed: 2026-09-22
