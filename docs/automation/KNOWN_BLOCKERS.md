# Car Comfort Known Blockers

## Automation Blockers

*To be populated during exploration and automation*

### Format
```
BLOCKER_ID:
  screen:
  element:
  available attributes:
  reason:
  recommended improvement:
  workaround:
  status: OPEN/IN_PROGRESS/RESOLVED/WONT_FIX
  discovered:
  owner:
```

---

## Current Blockers

### B-001: No physical Android device attached (infrastructure)
- screen: N/A (environment)
- element: N/A
- available attributes: `adb devices -l` returns empty list (verified 2026-09-22)
- reason: No USB/Wi-Fi Android device connected to the bootstrap machine; live Flutter app discovery and on-device execution impossible until hardware is attached
- recommended improvement: Attach an authorized physical device with USB debugging + Car Comfort app installed; set ANDROID_DEVICE_UDID / ANDROID_APP_PACKAGE / ANDROID_APP_ACTIVITY
- workaround: Framework smoke (config, locks, guards, locators, classification) verified without hardware; device-backed tests skip cleanly
- status: RESOLVED (device leg) — Xiaomi 22021211RI, Android 14, attached as `device` via USB on 2026-09-22; Car Comfort `io.carcomfort.app` v1.1.1 present with `.MainActivity`. Remaining: owner confirmation that this unit is the sanctioned test device before UI-driving exploration begins.
- discovered: 2026-09-22
- owner: bootstrap

### B-002: Customer transient incomplete-signup gate (vehicle + driver-license) — RESOLVED
- screen: AND-CUST-VEHICLE-001 → AND-CUST-LICENSE-001 (historical gate record; excluded from normal paths)
- element: `Save Vehicle` / `Cancel Signup` / `Upload Front/Back Image` (all GATED, never tapped — cancelTaps=0)
- root cause: test account remained in incomplete onboarding/signup state in one prior session
- resolution: transient state cleared without action; owner-authorized one-time Cancel Signup NOT needed and NOT executed (auth recorded unused); relogin → `Carl Customer!` home verified 2026-09-22 with full customer branch mapping (10 live screens)
- role correction: gate was CUSTOMER (post-customer-login evidence); owner "provider" label inaccurate — same wizard mechanism may exist per-role but observed only for customer
- ONBOARDING_RESET_COMPLETED: NOT_REQUIRED (no reset executed; future agents: if home login works, skip onboarding path entirely; never auto-run Cancel Signup)
- status: RESOLVED 2026-09-22 (record retained, not deleted)
- discovered: 2026-09-22
- owner: mapping-agent

### B-003: Flutter weak semantics on inputs + nav icons (informational, workaround verified)
- screen: login + home (both roles)
- element: Email/Password EditTexts (no hint/desc/id); avatar/bottom-nav/bell (no desc, positional instances, per-screen numbering)
- reason: stable automation needs class-instance + bounds re-query workarounds
- recommended improvement: add content-desc/hint ("Email Address", "Password", nav labels, avatar label)
- workaround: focus-tap + autofill BACK-dismiss (login); bottom-tab re-query by bounds (nav) — verified 2026-09-22
- status: WONT_FIX (app-side) / workaround VERIFIED
- discovered: 2026-09-22
- owner: mapping-agent