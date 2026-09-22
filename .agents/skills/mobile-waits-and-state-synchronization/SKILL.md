---
name: mobile-waits-and-state-synchronization
description: Use when synchronizing on any mobile or web state change, fixing a flaky wait, or reviewing timing logic in automation code.
---

# mobile-waits-and-state-synchronization

## When to use
Element/async/screen timing, loader handling, activity/context switches, flaky-test triage.

## Rules
- `Thread.sleep()` is forbidden for synchronization (`WaitStrategies.sleep()` throws by design).
- Never mix implicit and explicit waits: framework implicit wait is 0; all sync is explicit FluentWait (default 15s / long 30s / short 5s, 500ms poll).
- Wait for STATE, not seconds: present, visible, clickable, enabled, gone, text-changed, loader-gone (`waitForLoaderToDisappear`), activity/context changed, toast/snackbar, keyboard shown/hidden, URL/page-load for PWA.
- Network-idle = loader disappearance, never a fixed pause. Do not inflate global timeouts to hide product slowness.
- Flaky triage order: missing/wrong wait (SYNC_ISSUE) → drifting locator → real timing defect. Quarantine + root-cause instead of blind retries.

## Authoritative References

- Selenium waits — https://www.selenium.dev/documentation/webdriver/waits/
- Appium UiAutomator2 driver — https://github.com/appium/appium-uiautomator2-driver

Last reviewed: 2026-09-22
