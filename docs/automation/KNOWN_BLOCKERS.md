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