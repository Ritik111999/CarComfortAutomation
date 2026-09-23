# Booking Status Matrix (evidence-backed)

> One row per OBSERVED state. HYPOTHESIZED rows are clearly marked and gain their
> action cells only after live discovery. PII/amounts masked.

| State | Customer sees | Provider sees | Available Customer actions | Available Provider actions | Next states |
| ----- | ------------- | ------------- | -------------------------- | -------------------------- | ----------- |
| Confirmed / Not Assigned (OBSERVED) | Card: service "Car Wash", "Service Provider Not Assigned", "Confirmed", schedule "As Soon As Possible", Total "$35.35", View Service Details | On-Demand Map dispatch pin ("45 min"), bottom sheet with "Car Wash", "On-demand service", "$85.00", venue "BabulKheda, Narendra Nagar, Nagpur" | View Service Details | "Accept", "Reject" | ACCEPTED |
| Accepted / To start (OBSERVED) | Card in My Bookings, Provider assigned, "Confirmed" | Active trip map sheet, "To start", "15:00" countdown timer ("Start driving to pick-up before the timer ends."), "Carl Customer", "Details", "Message", "Call" | View Service Details, Message, Call | "Start", "Details", "Message", "Call", "Cancel Service" | IN_PROGRESS (Enroute) |
| Enroute to pick up (OBSERVED) | In-progress tracking / provider assigned | Mapbox navigation view, "Enroute to pick up customer vehicle", "Carl Customer", "Car Wash", "Details", "Message", "Call" | View Service Details, Contact | "Start navigation", "Arrived at service pickup location", "Cancel Service" | ARRIVED_AT_PICKUP (Boundary: Customer OTP + Photos) |
| Completed (OBSERVED) | Card: provider name/rating, "Completed", charge, schedule; View Service Details | "BOOKING STATUS Completed", payout ($124.99), route plan, after-service photos and final billing note | View Service Details | None (terminal) | HISTORY |
| Cancelled by service provider (OBSERVED) | Card: "Cancelled by service provider", "Cancelled", amount, schedule | Cancelled state | View Service Details | None (terminal) | — |

## Lifecycle CC-E2E-ACCEPT-001 Verified Transitions

1. `SUBMITTED -> Confirmed (Service Provider Not Assigned)`
2. `PROVIDER_RECEIVED -> Dispatched on Home Map Pin / Job Sheet`
3. `PROVIDER_ACCEPTED -> Accepted ("To start" + 15 min pickup countdown)`
4. `CUSTOMER_SYNC -> Verified Provider Assignment in My Bookings`
5. `PROVIDER_START -> "Enroute to pick up customer vehicle"`
6. `PROGRESSION_BOUNDARY -> "Arrived at service pickup location"` (stops at prerequisite: Customer OTP Pin verification + Before-service photo upload)

