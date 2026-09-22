---
name: appium-mcp-operator
description: Use when exploring the live Car Comfort Android app with Appium MCP, managing MCP sessions, or choosing MCP flags for token-efficient exploration.
---

# appium-mcp-operator

## When to use
AI-driven exploration and device control through the Appium MCP server (installed: appium-mcp 1.94.3, stdio, workspace config `.agents/mcp_config.json`).

## Verified tool surface (v1.94.3 — do not invent names)
- Sessions/devices: `select_device`, `appium_session_management`, `appium_mobile_device_info`, `appium_mobile_device_control`, `appium_geolocation`
- Elements: `appium_find_element`, `appium_get_active_element`, `appium_get_element_attribute`, `appium_get_text`, `generate_locators`, `appium_get_page_source`
- Actions: `appium_gesture`, `appium_drag_and_drop`, `appium_perform_actions`, `appium_set_value`, `appium_mobile_press_key`, `appium_mobile_keyboard`, `appium_mobile_clipboard`
- Device state: `appium_screenshot`, `appium_get_window_size`, `appium_orientation`, `appium_alert`, `appium_context`, `appium_app_lifecycle`, `appium_mobile_permissions`, `appium_driver_settings`, `appium_mobile_file`, `appium_screen_recording`
- Generation: `appium_generate_tests`
- iOS prep: `prepare_ios_simulator`, `appium_prepare_ios_real_device`
- Docs (opt-in): `appium_documentation_query`, `appium_skills`
- Vision `appium_ai` is NOT registered unless explicitly enabled (see policy below).

## Rules
- SAFE MODE: no account creation, OTP/KYC/Stripe, payments, deletions, or irreversible writes. Record side-effect risks as GATED instead.
- MCP is the exploration layer; the deterministic Java/TestNG framework stays the execution system.
- Prefer `NO_UI=true` for agent runs (verified 50–90% token saving; screenshots still saved to disk).
- Enable `APPIUM_MCP_EVIDENCE=true` for structured locator/element/timing evidence on find/gesture calls.
- Never expose MCP to untrusted users; keep `remoteServerUrl` in trusted config; use `REMOTE_SERVER_URL_ALLOW_REGEX` on shared infra.
- On client disconnect, MCP-owned sessions are deleted by default; use `APPIUM_MCP_ON_CLIENT_DISCONNECT=skip` only for streamable transports that reconnect.
- AI Vision stays OFF (`AI_VISION_ENABLED=false` default): hierarchy-first discovery; vision only with explicit config, never committed keys, never replacing stable locators with coordinates.
- Do not re-request huge page sources for already-catalogued screens.

## Authoritative References

- appium/appium-mcp README (install, env flags, tools) — https://github.com/appium/appium-mcp
- Appium docs — https://appium.io/docs/en/latest/

Last reviewed: 2026-09-22
