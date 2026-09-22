# Appium MCP — verified installation (2026-09-22)

## Installed
- Package: `appium-mcp@1.94.3` (global, `/opt/homebrew/bin/appium-mcp`)
- Docs package: `@appium/mcp-documentation@1.0.14` (global; deduped under appium-mcp)
- Server identity: `MCP Appium 1.94.3`, stdio transport
- Workspace config: `.agents/mcp_config.json` (Antigravity auto-discovers workspace MCP servers there)

## Verified tool surface
- Base: 31 tools (sessions, devices, elements, gestures, screenshots, recording, app lifecycle,
  permissions, contexts, `generate_locators`, `appium_generate_tests`, iOS prep tools).
  Full list is recorded in skill `appium-mcp-operator`; tool names were read from a live
  `tools/list` call, not copied from docs.
- With `APPIUM_MCP_DOCS_ENABLED=true` + docs package installed: 33 tools, adding
  `appium_documentation_query` and `appium_skills` (both verified present).
- `appium_ai` (vision) is NOT registered by default — matches the AI_VISION_ENABLED=false policy.
- `APPIUM_MCP_EVIDENCE=true` startup verified (structured evidence on find/gesture per upstream).

## Active flags (token optimization)
- `NO_UI=true` — text-only responses; screenshots still saved to
  `artifacts/evidence/mcp-screenshots/` (screenshot dir auto-created on first use).
- `APPIUM_MCP_EVIDENCE=true` — structured action evidence for deterministic debugging.
- Telemetry/OTEL left OFF (no need). Vision left OFF (explicit policy).

## Environment diagnostics
- `appium driver doctor uiautomator2`: 0 required fixes (optional only: bundletool.jar, gstreamer).
- No physical device attached — session-creation tools verified by registration only, never executed.

## Safety contract (unchanged)
- Safe-mode exploration only; MCP never bypasses `GatedTestGuard`, device locks, or secret masking.
- MCP-owned sessions are deleted on client disconnect (default); `skip` only for reconnecting stream transports.
- Remote servers: keep `remoteServerUrl` in trusted config; narrow with `REMOTE_SERVER_URL_ALLOW_REGEX` on shared infra.

## Sources
- https://github.com/appium/appium-mcp (README: install, env flags, tools, disconnect behavior)
- Google Antigravity MCP docs (workspace config path `.agents/mcp_config.json`)
