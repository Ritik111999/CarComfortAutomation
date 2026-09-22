# Business Action Registry

> One entry per state-changing business action. "Verified" means executed once on a
> designated booking with ledger + evidence — never re-derived from memory.

| Action | Role | Required state | Side effect | Repeatable | Cleanup | Automation | Authorization |
| ------ | ---- | -------------- | ----------- | ---------- | ------- | ---------- | ------------- |
| BOOKING_CREATE (wizard submit) | CUSTOMER | wizard complete, no open lifecycle | creates visible booking | NO (1/lifecycle) | n/a (lifecycle continues) | UNKNOWN (discovery next) | BUSINESS_CASE happy-path |
| BOOKING_ACCEPT | PROVIDER | SUBMITTED (designated booking, correlation proven) | leaves pool; customer status flips | NO (idempotent-skip if accepted) | n/a | UNKNOWN | BUSINESS_CASE happy-path |
| BOOKING_REJECT | PROVIDER | SUBMITTED (own reject-booking) | terminal | NO | n/a | UNKNOWN (later milestone) | separate case |
| SERVICE_START/PROGRESS_* | PROVIDER | ACCEPTED→… (one step at a time) | status advances both roles | NO (idempotent-skip if advanced) | n/a | UNKNOWN | BUSINESS_CASE happy-path |
| SERVICE_COMPLETE | PROVIDER | final pre-state | terminal; possible payment consequence (STOP if live charge) | NO | history verify | UNKNOWN | BUSINESS_CASE happy-path |
| BOOKING_CANCEL_CUSTOMER | CUSTOMER | per-scenario booking | terminal (+fee?) | NO | n/a | UNKNOWN (later) | separate case |
| BOOKING_CANCEL_PROVIDER | PROVIDER | per-scenario booking | terminal | NO | n/a | UNKNOWN (later) | separate case |
| PAYMENT_CONFIRM / WITHDRAW / REFUND | either | — | financial mutation | NO | — | FORBIDDEN here | separate gate |
| Cancel Signup (GATED_ONBOARDING_EXIT) | CUSTOMER | incomplete signup | clears onboarding | NO (auth unused, taps=0) | — | recorded, never auto-run | consumed/unused one-time |

Rules: check ledger + live status before acting (idempotency); on action timeout, INSPECT state (never blind-retry); mutation lock per booking; one agent per lifecycle object.
