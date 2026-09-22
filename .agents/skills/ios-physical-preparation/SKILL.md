---
name: ios-physical-preparation
description: Use when preparing the project or host for a future physical iPhone (not for starting product automation, which is out of scope until Android is stable).
---

# ios-physical-preparation

## When to use
Mac/Xcode readiness, WDA signing questions, device-trust setup.

## Physical iPhone requirements (current XCUITest guidance — verify via appium_skills before acting)
macOS + Xcode; trusted Mac/iPhone relationship; Developer Mode + Enable UI Automation on the device; real-device UDID; WebDriverAgent build with Apple development team + provisioning profile; WDA trust on device; real-device WDA reuse across sessions.
- MCP helper `appium_prepare_ios_real_device` can download/sign WDA and return session capabilities when the time comes.

## Rules
- iOS code stays isolated (`IosDriverManager`, `mobile/ios/`). Never assume Simulator (`simctl`, simulated devices).
- No iOS tests in active suites until physical-iPhone coverage is declared.

## Authoritative References

- Appium XCUITest driver — https://github.com/appium/appium-xcuitest-driver
- Appium real-device setup — https://appium.io/docs/en/latest/

Last reviewed: 2026-09-22
