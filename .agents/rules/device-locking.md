# Rule: Device locking is mandatory

## device-locking

- Before touching any physical device, acquire the lock via `DeviceManager.acquireDevice()` / `DeviceLockManager.acquireLock()`.
- Lock file: `artifacts/device-locks/<PLATFORM>:<UDID>.lock` (owner, timestamp, heartbeat every 10s, stale cleanup after 600s).
- Only ONE agent/process per device. If acquire fails, STOP — never force-take another owner's lock.
- Release in a `finally` path (`quitDriver()` releases; `DeviceLockManager.stop()` releases all).
