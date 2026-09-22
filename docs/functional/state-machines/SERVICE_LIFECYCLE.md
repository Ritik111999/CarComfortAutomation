# Service Lifecycle State Machine

> OBSERVED: provider detail route plan (PICKUP → STOP 1 EV CHARGING → STOP 2 CAR WASH →
> RETURN DROPOFF), 70min estimate, 3.7mi radius — the service has a routed structure.
> Progression verbs: UNKNOWN until the accepted test booking renders them (never invented).

```
ACCEPTED (TBD label)
  ↓ [UNKNOWN] dispatch/start verbs — discover live on CC-E2E-ACCEPT-001
IN_PROGRESS (substates TBD)
  ↓ [UNKNOWN] completion prerequisites (photos? notes? customer confirm?)
COMPLETED (OBSERVED terminal: after-service photos + final billing note)
```

Rule: capture state → single transition → verify BOTH roles → ledger → continue.
