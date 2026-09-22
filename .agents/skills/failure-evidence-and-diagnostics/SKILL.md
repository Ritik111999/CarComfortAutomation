---
name: failure-evidence-and-diagnostics
description: Use when any test fails, when deciding what evidence to attach, or when classifying a failure before repairing.
---

# failure-evidence-and-diagnostics

## When to use
Failure triage, evidence review, deciding repair-vs-defect.

## Evidence (correlate every failure)
Execution ID, test ID, role, platform, device (model/OS/UDID-masked), app version if discoverable, timestamp, previous action, current screen/activity, attempted locator, exception, screenshot, page source/hierarchy excerpt, Appium log excerpt, ADB/device state when relevant. Mask secrets. Avoid storing identical huge XML dumps repeatedly.

## Classification (minimum taxonomy → repair policy)
- PRODUCT_DEFECT (maps to code APPLICATION_DEFECT): file with defect evidence; never retry into green, never weaken expectations.
- AUTOMATION_DEFECT / LOCATOR_DRIFT (`NoSuchElement`, bad selector) / SYNCHRONIZATION_FAILURE (`Timeout`, `StaleElement`): auto-repairable.
- DEVICE_FAILURE / APPIUM_FAILURE (`NoSuchSession`, session lost, UiAutomator2 crash) / NETWORK_ENVIRONMENT_FAILURE / PERMISSION_STATE_FAILURE / TEST_DATA_FAILURE: infrastructure path via `physical-device-resilience`; record separately.
- UNKNOWN_REQUIRES_INVESTIGATION: park in KNOWN_BLOCKERS, do not guess-repair.

## Authoritative References

- Appium UiAutomator2 driver troubleshooting — https://github.com/appium/appium-uiautomator2-driver
- TestNG results — https://testng.org/

Last reviewed: 2026-09-22
