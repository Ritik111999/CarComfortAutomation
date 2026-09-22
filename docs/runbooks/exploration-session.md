# Runbook: Safe exploration session

1. Read `docs/automation/STATE.md` + coverage matrix; pick the next UNKNOWN flow.
2. Confirm `adb devices -l` shows your locked device; acquire the lock.
3. Prefer Appium MCP read-only tools first (page source, screenshot, hierarchy).
4. Navigate without side effects; stop at any form that writes production state → mark GATED.
5. Append SCREEN_CATALOG entries + update COVERAGE_MATRIX (DISCOVERED).
6. Release the lock; never leave heartbeat locks behind.
