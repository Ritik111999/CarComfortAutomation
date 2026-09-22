---
name: selenium-pwa-automation
description: Use when automating the Customer Booking PWA or Admin PWA in a desktop browser, or debugging a flaky Selenium wait there.
---

# selenium-pwa-automation

## When to use
PWA page objects, browser flows, PWA-specific waits and failures.

## Rules
- Selenium WebDriver + Java + TestNG via `WebDriverManager(customer|admin)`; never mobile Appium except when testing a browser inside the physical device.
- Base URLs from `CUSTOMER_PWA_URL` / `ADMIN_PWA_URL`; browser/timeout config from `carcomfort.web.*`.
- Reuse reporting, logging, test data, assertion standards, business-flow concepts, failure classification, and evidence architecture from mobile.
- Explicit waits only (URL, page-load, element state); flaky-wait triage per `mobile-waits-and-state-synchronization`.

## Authoritative References

- Selenium WebDriver — https://www.selenium.dev/documentation/webdriver/
- Selenium waits — https://www.selenium.dev/documentation/webdriver/waits/

Last reviewed: 2026-09-22
