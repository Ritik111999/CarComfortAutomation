---
name: appium-official-docs-research
description: Use when answering a detailed Appium question (capabilities, driver behavior, setup, errors) instead of relying on memory or local notes.
---

# appium-official-docs-research

## When to use
Any Appium technical question where upstream truth matters more than project convention.

## Rules
- Query `appium_documentation_query` (RAG over official Appium docs) first; use `appium_skills` for ordered setup/troubleshooting guidance.
- Requires `APPIUM_MCP_DOCS_ENABLED=true` + `@appium/mcp-documentation` installed (verified 1.0.14, tools appear only when both hold).
- Local skills describe HOW CAR COMFORT USES APPIUM, never duplicate the manual.
- If docs and local skill conflict, flag it in KNOWN_BLOCKERS/STATE rather than silently following one.

## Authoritative References

- @appium/mcp-documentation — https://github.com/appium/appium-mcp (Documentation Tools opt-in)
- Appium docs — https://appium.io/docs/en/latest/

Last reviewed: 2026-09-22
