---
name: ios-session-and-locator-engineering
description: Use when designing (not yet executing) XCUITest sessions or iOS locator strategy for the future physical iPhone.
---

# ios-session-and-locator-engineering

## When to use
Future XCUITest capability design, iOS locator reviews, WDA behavior questions.

## Rules (dormant until hardware exists)
- Sessions: `automationName=XCUITest`, real `udid`, signed WDA, `noReset=true`; keep Simulator-only caps out.
- Locators prefer iOS accessibility id, then predicate/class-chain with stable attributes; XPath last and constrained.
- Reuse the shared layers (waits, flows, reporting, evidence, gated guards) so iOS plugs in without rewriting business architecture.
- Confirm current driver details via `appium_documentation_query` before implementing — XCUITest behavior evolves.

## Authoritative References

- Appium XCUITest driver — https://github.com/appium/appium-xcuitest-driver
- Apple XCTest — https://developer.apple.com/documentation/xctest

Last reviewed: 2026-09-22
