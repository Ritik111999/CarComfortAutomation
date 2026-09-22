---
name: cross-role-e2e-lifecycle
description: Use when modeling or automating a lifecycle that spans Customer, Provider, and Admin roles.
---

# cross-role-e2e-lifecycle

## When to use
Flows like: customer initiates → provider sees expected state → provider acts → customer observes update → admin reflects authoritative state.

## Status: UNKNOWN / DISCOVERY_PENDING — lifecycle names and transitions must come from real discovery, never from this example.
- Independent sessions per role; no single hardcoded account.
- Assert each role's visible state per transition; mismatches are POTENTIAL_PRODUCT_DEFECT, never expectation edits.
- Destructive cancels are GATED unless covered by safe test-data rules.

## Authoritative References

- TestNG dependencies — https://testng.org/
- Selenium Page Objects — https://www.selenium.dev/documentation/test_practices/encouraged/page_object_models/

Last reviewed: 2026-09-22
