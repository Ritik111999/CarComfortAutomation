---
name: reporting-and-logging
description: Use when adding framework logs, wiring test reporting, or reading Extent, PDF, or JSON execution outputs.
---

# reporting-and-logging

## When to use
Log statements, report content, execution summaries, screenshot attachment.

## Rules
- SLF4J + Log4j2 only (`log4j2.xml`); no `System.out`. `TestLogger` events: TEST_START/END, SCREEN_OPEN, ACTION, ASSERTION, BUSINESS_CHECKPOINT, WARNING, FAILURE, DEVICE_FAILURE, FRAMEWORK_FAILURE. Never log secrets/PII.
- Tests use the `ReportEngine` abstraction (Extent HTML) — never Extent APIs in tests. `PdfReportGenerator` adds the PDF QA report; JSON summary where enabled.
- Reports must show PASS / FAIL / SKIP / BLOCKED / GATED_NOT_AUTHORIZED / INFRASTRUCTURE_FAILURE honestly. No false PASS. Screenshots: all key failures, checkpoints only on success.

## Authoritative References

- SLF4J — https://www.slf4j.org/
- Log4j — https://logging.apache.org/log4j/2.x/

Last reviewed: 2026-09-22
