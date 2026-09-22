# Car Comfort Gated Flows

## Gated Test Categories

### GATED
Any irreversible action that modifies production state.

### ONE_TIME
Account creation, initial setup that cannot be repeated.

### VERIFICATION
OTP, KYC, identity verification, Stripe verification.

### PAYMENT_SETUP
Card addition, bank account linking, Stripe onboarding.

### ACCOUNT_BOOTSTRAP
First-time role onboarding, initial configuration.

### DESTRUCTIVE
Account deletion, data purge, irreversible cancellations.

---

## Gated Flows Inventory

*To be populated during exploration*

### Format
```
FLOW_ID:
  name:
  category: [GATED|ONE_TIME|VERIFICATION|PAYMENT_SETUP|ACCOUNT_BOOTSTRAP|DESTRUCTIVE]
  role:
  platform:
  description:
  preconditions:
  authorization case:
  side effects:
  rollback possible:
  test accounts:
  notes:
```

---

## Customer Gated Flows

### GATE-CUST-SIGNUP-EXIT-001 — Cancel Signup (one-time onboarding exit; AUTHORIZED but NOT executed)
- category: GATED_ONBOARDING_EXIT (controlled account-state reset; excluded from regression, never auto-run)
- role: CUSTOMER | screens: AND-CUST-VEHICLE-001 | authorization: owner one-time 2026-09-22 for incomplete-signup exit only
- execution: NOT EXECUTED (cancelTaps=0; gate transiently absent at session start) — auth recorded unused
- related: Save Vehicle (ACCOUNT_BOOTSTRAP), license Uploads (VERIFICATION), Sign up now (ONE_TIME), Delete Account (DESTRUCTIVE) — all untouched

### GATE-CUST-BOOKING-SUBMIT-001 — Final booking submission boundary
- category: GATED_BUSINESS_ACTION | screens: wizard steps 2-4 + Review | actions: Confirm Booking / Book Now / Submit / Pay / Place Order (none observed tapped; wizard stopped before Next)
- related: Cancel Booking, payment cancellation — NOT authorized

### GATE-CUST-PAYMENT-001 — Payment Methods entry
- category: PAYMENT_SETUP | screen: profile/settings rows (presence-only)

---

## Provider Gated Flows

### GATE-PROV-STRIPE-001 — Stripe verification (backend-completed, no automation action)
- category: PAYMENT_SETUP + VERIFICATION | screen: AND-PROV-WALLET-001
- observed 2026-09-22: gate text gone; wallet renders live balances/payment-history async (view-only verified, amounts masked, zero taps). Verification completed outside automation; policy gates remain for any future Stripe/Withdraw/bank action.
### GATE-PROV-JOBS-001 — Accept / Reject / Complete / Cancel (policy-gated; no live jobs observed)
### GATE-PROV-ACCOUNT-001 — Delete Account (DESTRUCTIVE, settings row presence-only)

---

## Admin Gated Flows

*(To be discovered)*

---

## Authorization Matrix

| Gated Case | Description | Required Approval | Test Accounts |
|------------|-------------|-------------------|---------------|
| ACCOUNT_CREATE | New account registration | Owner | Dedicated test emails |
| PHONE_VERIFY | Phone OTP verification | Owner | Test phone numbers |
| EMAIL_VERIFY | Email verification | Owner | Test emails |
| KYC_SUBMIT | Identity document upload | Owner | Test documents |
| STRIPE_ONBOARD | Stripe Connect onboarding | Owner | Stripe test mode |
| CARD_ADD | Payment card addition | Owner | Stripe test cards |
| BANK_ADD | Bank account linking | Owner | Stripe test accounts |
| PROVIDER_VERIFY | Provider verification | Owner | Test provider accounts |
| ACCOUNT_DELETE | Account deletion | Owner + Written | Dedicated test accounts |
| DATA_PURGE | Data deletion | Owner + Written | Test data only |