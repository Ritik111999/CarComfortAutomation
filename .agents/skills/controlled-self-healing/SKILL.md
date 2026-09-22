---
name: controlled-self-healing
description: Use when deciding whether a failing test may be repaired automatically, or when writing tests that resist flakiness without hiding defects.
---

# controlled-self-healing

## When to use
Post-failure repair decisions, flakiness prevention, healing-policy reviews.

## Allowed (automation defects only)
Locator repair, wait repair, driver-config repair, navigation-implementation repair, test-data setup repair, framework defect repair — each followed by affected-test rerun + regression subset + doc updates.

## Forbidden
Changing an expected value because actual differed; removing assertions; catching failure and reporting PASS; altering requirements without evidence; blindly accepting new UI behavior; retrying assertion failures until green.

## Flakiness prevention
Explicit waits everywhere, stable locators, isolated data per test, no order dependence, bounded retries for proven-transient infra only, quarantine + root-cause over timeout inflation.

## Authoritative References

- Selenium waits — https://www.selenium.dev/documentation/webdriver/waits/
- Appium docs — https://appium.io/docs/en/latest/

Last reviewed: 2026-09-22
