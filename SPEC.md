# SPEC.md

> Stable project contract for Salon Manager. AI execution rules live in `AGENTS.md`; current tasks live in `TODO.md`; roadmap lives in `docs/21-项目全面诊断与方向.md`.

## 1. Project Scope

Salon Manager is a single-store salon SaaS prototype covering members, cashier/consumption, appointments, employees, services, inventory, coupons, marketing, reports, customer H5, payment notification, and AI trial capabilities.

Internal store-management flows are mostly closed enough for pilot validation. Current major gaps are tracked in `docs/21`, especially repository health, real payment/SMS, Redis production readiness, CI test confidence, and remaining money/audit risks.

## 2. Architecture And Stack

| Area | Contract |
|---|---|
| Backend | Spring Boot 3.2.0 + MyBatis Plus 3.5.5 + JDK 21 |
| Database | MySQL 8.x, database `salon` |
| Frontend | Vue 3 Composition API + Vite 5 + Element Plus + Pinia |
| AI | Spring Boot BFF + Python FastAPI |
| Backend port | `8080` |
| Frontend port | `3000` |
| AI port | `8000` |
| Default login | `admin / admin123` |

```text
backend/        Spring Boot backend
frontend/       Vue frontend
ai/             Python AI service
db/             data exports and migration-related files
docs/           detailed docs and historical decisions
docs/prd/       single-feature PRDs
TODO.md         current executable task list
```

## 3. API Contract

- Every business API path starts with `/api`.
- Frontend `request.js` has no `baseURL`; frontend calls use full `/api/...` paths.
- Backend Controller `@RequestMapping` starts with `/api`.
- Vite proxy forwards `/api` to `localhost:8080`.
- JWT interceptor matches `/api/**`.
- Backend response envelope is `{ code, msg, data }`.
- Frontend Axios interceptor returns the unwrapped `data`; business code must not call `.data` again.
- Controller boundaries never expose Entity types.
- POST/PUT input uses `@Valid @RequestBody XxxReqDTO`.
- GET/POST/PUT output uses `XxxVO`, `List<XxxVO>`, or `PageResult<XxxVO>`.
- DTO contains only fields the client may modify.
- VO contains only safe fields the client needs.
- Required DTO fields use `@NotBlank` / `@NotNull`.
- DTO/VO are module-local and are not reused across module boundaries.

Detailed API inventory: `docs/04-API接口详细文档.md`.

## 4. Backend Quality Contract

Concurrency and data integrity:

- Counters, balance, points, stock, issued quantity, and similar deltas use atomic SQL, not read-modify-write.
- Deduction SQL includes a lower-bound guard such as `AND balance >= ?`.
- State transitions include the old status in `WHERE`.
- Critical `jdbcTemplate.update()` calls check affected rows unless a comment explains why silent failure is acceptable.
- After `jdbcTemplate.update()`, do not use MyBatis to read the same entity inside the same transaction as the source of truth.
- Multi-row or multi-table writes use `@Transactional(rollbackFor = Exception.class)`.
- Exceptions inside transactional flows must propagate unless there is an explicit compensation or degradation design.

Money, idempotency, and lifecycle:

- Monetary calculations use `BigDecimal.setScale(2, RoundingMode.HALF_UP)`.
- Money movement includes atomic balance update, business log, and ledger/record.
- External callbacks are idempotent.
- Scheduled jobs are idempotent and use a lock for multi-instance safety.
- Time-limited states such as `expire_time` / `expire_date` have expiry handling.
- Derived fields have reconciliation SQL or equivalent verification.

Security and reliability:

- No production secrets, keys, tokens, passwords, or AccessKeys are hardcoded.
- SQL values are parameterized.
- Business errors use structured `BusinessException(ErrorCode.XXX)` where the project has an error code.
- Wrapper comparisons are null-safe, for example `Integer.valueOf(1).equals(status)`.
- External services such as SMS, payment, and AI use timeout and fallback behavior.

Full historical rules and examples: `docs/01-代码生成策略.md` and `docs/12-商用代码审计未修复项-2026-05-22.md`.

## 5. Frontend Contract

CSS layering:

```text
frontend/src/styles/variables.css          CSS variables only
frontend/src/styles/reset.css              reset, fonts, scrollbar
frontend/src/styles/element-overrides.css  Element Plus overrides
*.vue <style scoped>                       page/component styles
```

Rules:

- Every Vue component style is scoped.
- Colors, shadows, and radii use CSS variables.
- Element Plus overrides stay in `element-overrides.css`.
- Component class names are semantic.
- API calls use `try-catch` and show `ElMessage.error()` on failure.
- List/table pages provide loading, empty, and error states.
- Money/order/payment submit buttons disable during submit to reduce duplicate submissions.

Detailed style contract: `docs/02-前端样式策略.md`.

## 6. Database And Config Contract

- `backend/src/main/resources/schema.sql` is idempotent and repeatable.
- New tables/columns update `docs/05-数据库参考.md` and `docs/16-数据库变更管理策略.md`.
- `application.yml` holds common config; default profile is `dev`.
- `application-dev.yml` is for local development.
- `application-prod.yml` reads sensitive values from environment variables.
- Docker uses `SPRING_PROFILES_ACTIVE=prod`; the repo must keep a reproducible prod config template or equivalent instructions.
- Data exports under `db/` must be checked and sanitized before commit, especially `audit_log` request params, passwords, tokens, and phone numbers.

Deployment details: `docs/15-部署与运维手册.md`.

## 7. Verification Contract

Choose verification based on blast radius:

```text
Backend: mvn test, or at minimum mvn compile for narrow doc/config-free changes
Frontend: npm run build
API: run backend and verify key endpoints with curl or Swagger
UI: run Vite and verify main browser interactions
Docker/deploy: run docker compose smoke when deployment behavior changes
```

High-risk flows require failure-path verification: money, appointments, inventory, coupons, payment callbacks, scheduled jobs, concurrency guards, idempotency, and duplicate submit behavior.

## 8. Documentation Contract

| Trigger | Update |
|---|---|
| New/changed Controller endpoint | `docs/04-API接口详细文档.md` |
| New enum or status field | `docs/05-数据库参考.md` |
| New table or column | `docs/16-数据库变更管理策略.md` + `docs/05-数据库参考.md` |
| Business flow change | `docs/06-核心业务流程.md` |
| New page or route | `docs/07-前端路由与组件清单.md` |
| Bug fix or new technical debt | `docs/13-已知问题与技术债务.md` |
| Architecture or middleware decision | `docs/10-开发策略与优先级.md` + relevant `docs/12` section |
| Roadmap or priority change | `docs/21-项目全面诊断与方向.md` |
| Current executable task change | `TODO.md` |
| New large feature requirement | `docs/prd/PRD-功能名.md` |

## 9. Document Map

- `AGENTS.md`: AI execution rules.
- `SPEC.md`: stable project contract.
- `TODO.md`: current executable checklist.
- `docs/21-项目全面诊断与方向.md`: diagnosis, priorities, roadmap.
- `docs/prd/`: single-feature PRDs.
- `docs/04-API接口详细文档.md`: detailed API inventory.
- `docs/05-数据库参考.md`: database tables, fields, enums, statuses.
- `docs/06-核心业务流程.md`: core business flows.
- `docs/12-商用代码审计未修复项-2026-05-22.md`: commercial audit and production architecture gaps.
- `docs/13-已知问题与技术债务.md`: known issues and technical debt.
- `docs/15-部署与运维手册.md`: deployment, Docker, migration, operations.
- `docs/17-AI集成技术方案.md`: AI integration design.
- `docs/18-API修复策略.md`: API path, proxy, and response-unpacking troubleshooting.
- `docs/20-预约日历看板需求文档.md`: appointment calendar acceptance reference.
