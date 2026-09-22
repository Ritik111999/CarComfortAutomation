# Rule: Secrets and evidence

## secrets-and-evidence

- No credentials, tokens, Stripe keys, signing passwords, or PII in code, docs, or logs. Env/config only; `.env` stays uncommitted.
- Mask via `TestLogger.mask()` / `TestDataManager.maskForLogging()`; secret patterns are configured centrally.
- On failure auto-capture: screenshot, page source, stack trace, screen/activity/context, device info, previous action, masked test-data IDs.
- Success screenshots only at business checkpoints.
