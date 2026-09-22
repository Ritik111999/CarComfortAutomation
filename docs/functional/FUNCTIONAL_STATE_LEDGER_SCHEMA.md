# Functional State Ledger Schema

> Runtime file (git-ignored): `artifacts/runtime/FUNCTIONAL_STATE_LEDGER.json`.
> Written by `FunctionalStateLedger` immediately after every verified transition.

```json
{
  "CC-E2E-ACCEPT-001": {
    "lifecycleId": "CC-E2E-ACCEPT-001",
    "testRunId": "exec-local-YYYYMMDD-HHMMSS",
    "customerAccountAlias": "test-customer",
    "providerAccountAlias": "test-provider",
    "serviceType": "Car Wash",
    "scheduledDate": "YYYY-MM-DD",
    "scheduledDateOffsetDays": 21,
    "timeLabel": "<as shown by app>",
    "bookingId": "#CC-... (once revealed by UI)",
    "currentBusinessState": "SUBMITTED | ACCEPTED | ... | COMPLETED",
    "lastAction": "CUSTOMER_SUBMIT",
    "lastActor": "CUSTOMER",
    "nextExpectedAction": "PROVIDER_ACCEPT",
    "cleanupStatus": "OPEN | DONE",
    "createdAt": "<ISO>",
    "updatedAt": "<ISO>"
  }
}
```

Rules: aliases only (no passwords/tokens/cards); `bookingId` filled when UI reveals it;
`currentBusinessState` uses matrix labels (HYPOTHESIZED labels graduate to OBSERVED on
first evidence); resume = read state → perform `nextExpectedAction` only.
