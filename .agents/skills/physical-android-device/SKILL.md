---
name: physical-android-device
description: Use when detecting, authorizing, selecting, or recovering a real physical Android test device over ADB, or diagnosing device visibility problems.
---

# physical-android-device

## When to use
`adb devices -l` shows nothing / unauthorized / offline; choosing the session device; USB or deliberate Wi-Fi debugging setup.

## Rules
- Physical devices only. Capability block must never contain `avd`, `avdLaunchTimeout`, or `emulator-5554` assumptions.
- Identify the session device with `appium:udid` (required). `appium:deviceName` is cosmetic, never the selection key.
- Required state knowledge: `adb devices -l` output, USB-debugging authorization prompt, `device` vs `unauthorized` vs `offline`, platform version (`ro.build.version.release`), model, app package/activity discovery via `adb shell cmd package` / `dumpsys`.
- Recovery: `adb reconnect`, re-plug/re-authorize RSA prompt, check screen-lock state, confirm ownership lock before use.
- Environment diagnostics: `appium driver doctor uiautomator2` (verified: 0 required fixes on bootstrap host; optional bundletool/gstreamer only).
- UDIDs and app ids come from env/config, never hardcoded in Java.

## Authoritative References

- Appium UiAutomator2 driver — https://github.com/appium/appium-uiautomator2-driver
- Android Developers: ADB — https://developer.android.com/tools/adb

Last reviewed: 2026-09-22
