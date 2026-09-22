# Multi-Agent Coordination (Antigravity / Codex / OpenCode)

## Device ownership
- One physical device = one owner at a time (`AUTOMATION_OWNER_ID`: antigravity | codex | opencode | human).
- Acquire the file lock before any device/Appium action; release in `finally`. Never steal another owner's lock.
- Check `artifacts/device-locks/` before starting; stale locks (>600s) are cleaned by the heartbeat.

## Code collaboration
- Significant concurrent work → isolated branches/worktrees (e.g. `codex/flow-cust-004`, `opencode/fix-locator-12`).
- Never commit on another agent's branch; never force-push.
- Logical roles: Explorer (read-only discovery), Automation Architect, Android Engineer, Web Engineer, Test Designer, Debugger, Coverage Auditor, Reviewer (read-only preferred).

## Memory discipline
- `AGENTS.md` = permanent rules (rarely changes).
- `docs/automation/STATE.md` = current state (update every session: what ran, result, next action).
- Coverage/catalogs updated with every automation change — no silent work.

## Token efficiency
After bootstrap, these commands imply the full standard set (rules + skills + workflows):
`Continue Android automation from verified state.` / `Explore the next uncovered Customer flow.` /
`Automate the next Provider flow.` / `Run regression and repair automation defects only.` /
`Audit current coverage.` / `Run gated CARD_SETUP only.`
