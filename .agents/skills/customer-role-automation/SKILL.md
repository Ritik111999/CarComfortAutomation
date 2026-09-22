---
name: customer-role-automation
description: Use when planning, discovering, or automating Customer-side Car Comfort behavior in the app or Customer PWA.
---

# customer-role-automation

## When to use
Browse/search services, booking wizard, my bookings, history, notifications, reversible profile edits, support.

## Status: UNKNOWN / DISCOVERY_PENDING (no device attached yet — do not fabricate screens)
- Discover from the live app first (SCREEN_CATALOG), then model, then automate.
- Independent customer session per test; cancellations only under safe test-data rules; account deletion is GATED.
- Update COVERAGE_MATRIX (CUST-*) + FLOW_CATALOG per addition.

## Authoritative References

- Flutter accessibility — https://docs.flutter.dev/ui/accessibility
- Selenium Page Objects — https://www.selenium.dev/documentation/test_practices/encouraged/page_object_models/

Last reviewed: 2026-09-22
