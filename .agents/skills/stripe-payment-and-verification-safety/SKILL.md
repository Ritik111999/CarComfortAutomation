---
name: stripe-payment-and-verification-safety
description: Use when investigating a failed Stripe sandbox payment test, modeling price assertions, or handling any card, payout, or identity-verification path.
---

# stripe-payment-and-verification-safety

## When to use
SANDBOX_PAYMENT_TEST triage, price-mismatch analysis, Stripe Identity test-mode planning.

## Rules
- Sandbox only in normal runs (`STRIPE_*_TEST` keys, official test card numbers/Identity fixtures from docs.stripe.com). Never use real PAN/CVV or live keys for automation.
- Distinguish SANDBOX_PAYMENT_TEST (repeatable) from GATED_PRODUCTION_CHECK (owner-authorized, dual-gate). Live financial actions need explicit owner authorization.
- Price mismatch (expected ₹500 vs actual ₹700) is POTENTIAL_PRODUCT_DEFECT — never adjust the expected value to pass.
- Never log or commit card details, CVV, secrets, or identity documents.

## Authoritative References

- Stripe testing — https://docs.stripe.com/testing
- Stripe Identity testing — https://docs.stripe.com/identity/test-mode

Last reviewed: 2026-09-22
