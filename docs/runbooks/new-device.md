# Runbook: New physical Android device

1. Enable USB debugging on the device; connect via USB (or ADB-over-Wi-Fi later).
2. `adb devices -l` → state must be `device` (not unauthorized/offline).
3. Export env: `ANDROID_DEVICE_UDID`, `ANDROID_DEVICE_NAME`, `ANDROID_APP_PACKAGE`, `ANDROID_APP_ACTIVITY`, `AUTOMATION_OWNER_ID`.
4. `scripts/device-status.sh` → confirm visibility + no stale locks.
5. Start Appium (`appium` or autoStart) with UiAutomator2 driver installed.
6. Run safe smoke: `scripts/run-smoke.sh` (device-backed tests only where implemented).
7. Lock discipline: one owner at a time; locks auto-release on `quitDriver()` / suite teardown.
