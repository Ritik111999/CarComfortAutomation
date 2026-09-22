---
name: gated-one-time-flow-safety
description: Use when touching or running any one-time, verification, payment-setup, or destructive flow, or when deciding whether a newly discovered action is gated.
---

# gated-one-time-flow-safety

## When to use
Account creation, OTP/email verification, provider verification, KYC, Stripe onboarding/verification, card/bank setup, live payments, account deletion, irreversible config. Also the `Run gated CARD_SETUP only` command.

## Rules (no exceptions, no self-authorization)
- Categories GATED / ONE_TIME / VERIFICATION / PAYMENT_SETUP / ACCOUNT_BOOTSTRAP / DESTRUCTIVE are disabled by default and excluded from smoke, regression, CI, exploratory, and repair runs.
- Execution needs BOTH `RUN_GATED_TESTS=true` AND a matching `GATED_CASE`; `GatedTestGuard` aborts before any device action otherwise. Entry point: `scripts/run-gated.sh <CASE>`.
- An AI agent cannot authorize itself. Normal flows must never indirectly invoke gated steps.
- Record new gated flows in `docs/automation/GATED_FLOWS.md` + `gated-cases.yaml`.

## Authoritative References

- Stripe testing — https://docs.stripe.com/testing
- Appium docs: keeping runs safe — https://appium.io/docs/en/latest/

Last reviewed: 2026-09-22
