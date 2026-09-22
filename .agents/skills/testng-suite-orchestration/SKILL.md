---
name: testng-suite-orchestration
description: Use when creating TestNG suites, groups, parameters, data providers, dependencies, or retry logic, or when deciding whether tests may run in parallel.
---

# testng-suite-orchestration

## When to use
New suite XML, group design, data-driven tests, retry policy, parallelism questions.

## Groups
smoke, regression, android, ios, customer, provider, admin, pwa, e2e, payment-sandbox, gated, one-time, verification, destructive. Normal suites exclude all gated groups; only `gated-one-time.xml` includes them (entries stay disabled without guard authorization).

## Rules
- Tests extend `BaseTest`; suite resources are static and shared; mobile suites keep `parallel="false"`.
- Data providers for role/data matrices; parameters (`deviceUdid`) from suite XML, never hardcoded.
- Retry ONLY for classified transient infrastructure/driver conditions (`carcomfort.recovery.*`); repeatable assertion failures are never retried. Retries are recorded visibly in the report.
- Mobile parallelism stays OFF: one agent/process per physical device (file lock). PWA parallelism only after proven session/data isolation.

## Authoritative References

- TestNG documentation — https://testng.org/
- Maven Surefire — https://maven.apache.org/surefire/maven-surefire-plugin/

Last reviewed: 2026-09-22
