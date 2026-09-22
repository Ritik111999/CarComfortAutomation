# Rule: No Thread.sleep, no coordinates

## no-sleep-no-coordinates

- `Thread.sleep()` is forbidden for synchronization. Use `WaitStrategies` / `AndroidWaitStrategies` (Awaitility-backed server polling is the only exception, inside `AppiumServerManager`).
- `WaitStrategies.sleep()` throws `UnsupportedOperationException` by design — do not work around it.
- Coordinate/image taps are forbidden as a primary strategy. Follow the locator priority: accessibility-id > resource-id > stable text > UiAutomator > constrained XPath.
- Network-idle = wait for a loader to disappear, never a fixed pause.
