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

*(To be discovered)*

---

## Provider Gated Flows

*(To be discovered)*

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