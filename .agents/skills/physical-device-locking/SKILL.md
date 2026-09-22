---
name: physical-device-locking
description: Use before creating any mobile session, when a device looks busy, or when handling abnormal termination of a device run.
---

# physical-device-locking

## When to use
Session setup/teardown, lock contention, stale-lock triage.

## Protocol (mandatory mutex)
ACQUIRE DEVICE LOCK → verify device identity → verify session ownership → run → cleanup safely → RELEASE LOCK (always in `finally`; `quitDriver()` and suite teardown release).

## Rules
- Lock file: `artifacts/device-locks/<PLATFORM>:<UDID>.lock`, owner + timestamp, 10s heartbeat, 600s stale cleanup.
- One owner per device (`AUTOMATION_OWNER_ID`). Contention means STOP, never force-take.
- Stale-lock recovery only after proving no live Appium session still owns the device. Never delete a lock just to gain access.

## Authoritative References

- Appium session management — https://appium.io/docs/en/latest/
- Android Developers: ADB — https://developer.android.com/tools/adb

Last reviewed: 2026-09-22
