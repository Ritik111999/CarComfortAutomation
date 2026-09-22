---
name: mobile-gesture-and-scroll
description: Use when scrolling a list, swiping a carousel, dismissing a sheet, or performing drag-and-drop on the physical Android device.
---

# mobile-gesture-and-scroll

## When to use
Scrollable containers, swipeable cards, drag handles, pull-to-refresh.

## Rules
- Use `GestureHelper` (W3C actions; durations from `carcomfort.android.gestures.*`).
- Prefer `scrollUntilVisible(locator, maxSwipes)` (explicit presence check per swipe) over fixed swipe counts.
- Element-anchored gestures beat screen-percentage swipes; coordinate taps are never a primary strategy.
- MCP path uses the verified `appium_gesture` / `appium_drag_and_drop` / `appium_perform_actions` tools.

## Authoritative References

- Selenium actions API — https://www.selenium.dev/documentation/webdriver/actions_api/
- Appium gestures — https://appium.io/docs/en/latest/

Last reviewed: 2026-09-22
