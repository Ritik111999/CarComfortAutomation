---
name: test-data-and-state-isolation
description: Use when adding test data, wiring test accounts, isolating state between tests, or handling anything secret, credential-like, or PII-adjacent.
---

# test-data-and-state-isolation

## When to use
YAML data files, account/session setup, secret masking reviews, leakage triage.

## Rules
- Data in `src/test/resources/testdata/*.yaml` via `TestDataManager` with `${ENV_VAR}` placeholders; role sessions stay independent; no shared hardcoded account in test classes.
- Never hardcode or commit credentials, API/Stripe keys, tokens, signing passwords, card data, identity documents, or PII. `.env` stays uncommitted; `.env.example` holds placeholders.
- Mask via `TestLogger.mask()` / `maskForLogging()`; central `secretPatterns` cover logs and reports. Suspected leaks are blockers.

## Authoritative References

- TestNG parameters — https://testng.org/
- Stripe testing — https://docs.stripe.com/testing

Last reviewed: 2026-09-22
