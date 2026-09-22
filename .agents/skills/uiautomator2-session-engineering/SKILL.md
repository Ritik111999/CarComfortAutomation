---
name: uiautomator2-session-engineering
description: Use when creating, debugging, or reviewing an Appium UiAutomator2 session against a physical Android device, including capabilities, ADB state, app lifecycle, and driver failures.
---

# uiautomator2-session-engineering

## When to use
Session fails to start; choosing capabilities; Appium server vs embedded-driver decisions.

## Rules
- Two modes: embedded local drivers (MCP-managed, no global server needed) vs remote Appium server (`remoteServerUrl` + explicit capabilities incl. `appium:udid`).
- Java framework path owns its server via `AppiumServerManager` (127.0.0.1:4723, Awaitility `/status` poll, never `Thread.sleep`).
- Base capabilities: `automationName=UiAutomator2`, `platformName=Android`, `appium:udid`, `appium:appPackage`/`appActivity`, `noReset=true` (never auto-wipe production state), `newCommandTimeout=300`.
- Query `appium_skills` for current ordered setup/troubleshooting before inventing fixes.
- Diagnose startup failure in order: server reachable → driver installed (`appium driver list --installed`) → device `device`-state + authorized → capabilities valid → UiAutomator2 server on device healthy.

## Authoritative References

- Appium UiAutomator2 driver — https://github.com/appium/appium-uiautomator2-driver
- Appium capabilities — https://appium.io/docs/en/latest/guides/caps/

Last reviewed: 2026-09-22
