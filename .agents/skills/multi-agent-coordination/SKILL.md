---
name: multi-agent-coordination
description: Use when starting work in the shared repo, coordinating with other agents, reviewing automation changes, or updating shared project memory.
---

# multi-agent-coordination

## When to use
Session start (read STATE/coverage/diff), concurrent implementation, code review, state updates.

## Rules
- Antigravity, Codex, and OpenCode share one repo; files are canonical memory. Every session: read STATE.md, relevant skill(s), coverage; inspect `git status/diff`; update state after verified changes.
- Isolated branches/worktrees for concurrent implementation; never overwrite another agent's work or push to their branch.
- Roles: Explorer (read-only), Architect, Android/Web Engineer, Test Designer, Debugger, Coverage Auditor, Reviewer (read-only preferred).
- Review bar: no sleeps/coordinates/hardcoded devices/secrets, layered architecture intact, gated groups excluded, evidence + logging present, docs updated, smoke green, failures classified.

## Authoritative References

- Google Antigravity MCP/skills — https://antigravity.google/docs/mcp
- TestNG — https://testng.org/

Last reviewed: 2026-09-22
