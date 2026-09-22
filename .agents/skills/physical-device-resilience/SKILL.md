---
name: physical-device-resilience
description: Use when a run fails for device, session, network, or permission-state reasons rather than product behavior.
---

# physical-device-resilience

## When to use
Disconnects, unauthorized/offline ADB states, screen lock, Appium session loss, UiAutomator2 server failure, keyboard/app-background interference.

## Rules
- Diagnose with `DeviceResilience.diagnose()` and record INFRASTRUCTURE_FAILURE separately from product failures.
- Safe recovery hints only (reconnect ADB, re-authorize, recreate session once, hide keyboard, foreground app). One careful retry for proven-transient infra; never retry assertion failures into green.
- Screen-locked devices: unlock flow must be documented; never bypass lock security silently.

## Authoritative References

- Android Developers: ADB — https://developer.android.com/tools/adb
- Appium UiAutomator2 driver — https://github.com/appium/appium-uiautomator2-driver

Last reviewed: 2026-09-22
