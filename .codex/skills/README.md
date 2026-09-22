# Codex skills — Car Comfort

Canonical skill content lives in `.agents/skills/<name>/SKILL.md` (30 skills).
Codex agents should load skills from there; this directory only records the mapping.

| Codex skill request | Canonical skill |
|---|---|
| device setup / adb | `physical-android-device`, `uiautomator2-session-engineering` |
| explore app | `appium-mcp-operator`, `flutter-semantics-locator-engineering` |
| write screen/flow/test | `page-component-flow-architecture`, `flutter-semantics-locator-engineering`, `mobile-waits-and-state-synchronization` |
| web PWA | `selenium-pwa-automation` |
| triage failure | `failure-evidence-and-diagnostics`, `controlled-self-healing` |
| gated flow | `gated-one-time-flow-safety` (authorization mandatory) |
| Appium how-to | `appium-official-docs-research` (`appium_documentation_query` / `appium_skills`) |

Multi-agent rules: isolated branches/worktrees per agent, one device owner at a time, Reviewer stays read-only.
