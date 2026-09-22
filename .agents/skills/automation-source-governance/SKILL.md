---
name: automation-source-governance
description: Use when deciding which documentation or guidance to trust for any Car Comfort automation question, or when adding references to a skill.
---

# automation-source-governance

## When to use
Deciding what to trust: API behavior, capability names, install commands, locator strategy, signing steps.

## Rules
- First-party sources are canonical: Appium docs + appium/appium, appium-uiautomator2-driver, appium-xcuitest-driver, appium-mcp, appium-mcp-documentation repos; developer.android.com; docs.flutter.dev; selenium.dev; testng.org; maven.apache.org; slf4j.org; logging.apache.org; docs.stripe.com; codelabs.developers.google.com.
- Blogs, Stack Overflow, random repos, and generated tutorials are supplementary only and never override first-party docs.
- For live Appium questions prefer the `appium_documentation_query` / `appium_skills` MCP tools over memory.
- Every project skill ends with Authoritative References + Last reviewed date. Never paste whole manuals into skills.

## Authoritative References

- Appium docs — https://appium.io/docs/en/latest/
- MCP Apps / protocol — https://modelcontextprotocol.io/

Last reviewed: 2026-09-22
