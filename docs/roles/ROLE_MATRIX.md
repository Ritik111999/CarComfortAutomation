# Car Comfort Role Matrix

## Roles & Capabilities

| Capability | Customer | Service Provider | Admin |
|------------|----------|------------------|-------|
| **Authentication** | | | |
| Login/Logout | ✅ | ✅ | ✅ |
| Register Account | ✅ | ✅ (invite) | ❌ |
| OTP/Email Verification | ✅ | ✅ | ✅ |
| Social Login | TBD | TBD | ❌ |
| Biometric Auth | TBD | TBD | ❌ |
| **Service Discovery** | | | |
| Browse Services | ✅ | ✅ (own) | ✅ (all) |
| Search/Filter | ✅ | ✅ | ✅ |
| View Details | ✅ | ✅ | ✅ |
| **Booking** | | | |
| Create Booking | ✅ | ❌ | ❌ |
| View Own Bookings | ✅ | ✅ (assigned) | ✅ (all) |
| Modify Booking | ✅ (rules) | ✅ (assigned) | ✅ |
| Cancel Booking | ✅ (rules) | ✅ (assigned) | ✅ |
| Accept/Reject Booking | ❌ | ✅ | ❌ |
| Complete Booking | ❌ | ✅ | ✅ |
| **Profile** | | | |
| View Profile | ✅ | ✅ | ✅ |
| Edit Profile | ✅ | ✅ | ✅ |
| Manage Payment Methods | ✅ | ❌ | ✅ |
| Manage Payout Methods | ❌ | ✅ | ✅ |
| **Notifications** | | | |
| Push Notifications | ✅ | ✅ | ✅ |
| In-App Notifications | ✅ | ✅ | ✅ |
| Email Notifications | ✅ | ✅ | ✅ |
| **Ratings/Reviews** | | | |
| Rate Provider | ✅ | ❌ | ✅ |
| Rate Customer | ❌ | ✅ | ✅ |
| View Reviews | ✅ | ✅ | ✅ |
| **Support** | | | |
| Contact Support | ✅ | ✅ | ✅ |
| View Tickets | ✅ | ✅ | ✅ |
| Manage Tickets | ❌ | ❌ | ✅ |
| **Admin Functions** | | | |
| User Management | ❌ | ❌ | ✅ |
| Provider Management | ❌ | ❌ | ✅ |
| System Configuration | ❌ | ❌ | ✅ |
| Analytics/Reports | ❌ | ✅ (own) | ✅ |
| Financial Oversight | ❌ | ❌ | ✅ |

## Platform Coverage by Role

| Platform | Customer | Provider | Admin |
|----------|----------|----------|-------|
| Android App | Primary | Primary | Secondary |
| iOS App | Primary | Primary | Secondary |
| Customer PWA | Primary | N/A | N/A |
| Admin PWA | N/A | N/A | Primary |

## Test Account Strategy

- **Separate accounts per role** - No shared credentials
- **Environment-specific** - Dev/Staging/Prod test accounts
- **Data isolation** - Each test run uses clean or known state
- **Gated accounts** - Real verification accounts require explicit authorization

## Cross-Role Validation Points

| Flow | Customer State | Provider State | Admin State |
|------|----------------|----------------|-------------|
| Booking Created | Pending | New Request | Visible |
| Booking Accepted | Confirmed | Accepted | Visible |
| Booking In Progress | In Progress | In Progress | Visible |
| Booking Completed | Completed | Completed | Visible |
| Booking Cancelled | Cancelled | Cancelled | Visible |
| Payment Made | Paid | Pending Payout | Transaction Log |
| Payout Processed | N/A | Paid Out | Reconciled |