---
name: android-system-dialogs-and-permissions
description: Use when a system permission dialog, OS popup, multi-window overlay, or the soft keyboard blocks a physical Android test.
---

# android-system-dialogs-and-permissions

## When to use
`permission_allow_button` flows, runtime-permission triage, keyboard-obscured taps, elements living in a second window.

## Rules
- Handle OS permission controllers only via `PermissionHandler.handlePermissionIfPresent(grant)` (resource-ids under `com.android.permissioncontroller` / `packageinstaller`, else stable Allow/Deny text).
- Never auto-accept product auth, KYC, payment, or destructive confirmations.
- Multi-window/system-dialog lookup: modern UiObject2-backed search spans windows; legacy `UiSelector`-only paths can miss the dialog window — scope the selector to the dialog and verify with hierarchy before falling back to constrained XPath.
- Hide the keyboard before tapping obscured elements; record permission requirements in the SCREEN_CATALOG entry.

## Authoritative References

- Android Developers: permissions — https://developer.android.com/guide/topics/permissions/overview
- Appium UiAutomator2 driver — https://github.com/appium/appium-uiautomator2-driver

Last reviewed: 2026-09-22
