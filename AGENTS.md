# AGENTS.md

> AI execution rules for this project. Project contracts live in `SPEC.md`; do not duplicate them here.

## 1. Required Reading

Before any non-trivial task:

1. Read [`SPEC.md`](SPEC.md).
2. Read task-specific docs linked from `SPEC.md` only when the task needs them.
3. For code changes, read the relevant source files before proposing edits.

`AGENTS.md` controls how the AI works. `SPEC.md` controls how the system is designed. If the two appear to conflict, stop and ask before changing code.

## 2. Thinking Rules

- Do not assume silently. State assumptions and tradeoffs.
- If the request has multiple valid interpretations, present them.
- Prefer the smallest change that solves the requested problem.
- Push back on speculative scope, risky shortcuts, or unclear requirements.
- If a simpler path exists, say so.

## 3. Change Discipline

- Touch only files needed for the task.
- Do not refactor adjacent code unless required.
- Match existing style and module boundaries.
- Preserve user changes and unrelated dirty worktree state.
- Remove only unused code introduced by your own change.
- Do not revert, reset, or delete user work without explicit approval.

## 4. Development Gate

For non-trivial implementation tasks, do not write code until this gate is complete and the user approves the plan:

1. **Context**: summarize the relevant project contract from `SPEC.md` and any task-specific docs.
2. **Data contract**: list every new/changed API path, request shape, response shape, and database table/column change.
3. **Files**: list backend, frontend, config, database, and docs files expected to change.
4. **Risks**: flag assumptions, ambiguous points, compatibility concerns, and verification plan.
5. **Approval**: wait for the user to confirm or correct the plan.

After approval, execute one module at a time. Default order for feature work:

```text
contract -> frontend -> backend -> verification -> docs
```

Do not generate frontend and backend code simultaneously for the same feature.

## 5. Verification Discipline

Code is not done until behavior is verified.

- Choose verification based on the change scope defined in `SPEC.md`.
- Prefer tests that reproduce the bug or cover the new behavior.
- For API/UI changes, verify the running backend and frontend when feasible.
- Report exactly what passed, what failed, and what was not run.
- Do not claim completion from compile/build alone when runtime behavior matters.

## 6. Documentation Discipline

- Stable project rules belong in `SPEC.md`.
- Current execution tasks belong in `TODO.md`.
- Roadmap and priority decisions belong in `docs/21-项目全面诊断与方向.md`.
- Single-feature product requirements belong in `docs/prd/`.
- Detailed API/database/business docs stay in their dedicated `docs/` files.
- After completing a change, update the docs required by `SPEC.md`.

## 7. Review Mode

When asked for a review, lead with findings:

1. Bugs, regressions, security risks, data consistency risks, and missing tests.
2. File and line references.
3. Severity ordered from highest to lowest.
4. Open questions or assumptions.
5. Brief summary only after findings.

If no issues are found, say that clearly and mention remaining test gaps.

## 8. Tooling Rules

- Use `rg`/`rg --files` first for search when available.
- Use `apply_patch` for manual file edits.
- Do not use destructive git commands unless explicitly requested.
- Do not install dependencies or access the network unless needed and approved.
- Prefer project-local scripts and existing tooling over new ad hoc tooling.

## 9. Completion Report

Final responses should be concise and include:

- What changed.
- Where it changed.
- Verification performed.
- Anything not done or intentionally deferred.
