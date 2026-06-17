# TODO.md

> 当前执行清单。路线图和优先级来源于 `docs/21-项目全面诊断与方向.md`；稳定系统契约见 `SPEC.md`。

## 使用规则

- 这里只放可执行任务，不放长期规范。
- 大功能先在 `docs/prd/` 写 PRD，再拆到这里。
- 工程修复、数据清理、CI、部署类任务可以直接放这里。
- 每完成一项，同步更新相关文档；路线图变化同步 `docs/21`。

## Now

- [x] 完成仓库扁平化迁移：根目录结构、根级 CI、`.gitignore` 清理一次性提交。
- [x] 修正 `application-prod.yml` 模板/提交策略，确保 Docker `prod` profile 可复现启动。
- [x] 脱敏或排除 `db/` 数据导出中的审计请求参数、测试密码、token 等敏感样例。
- [x] CI 从 `mvn compile` 升级为 `mvn test`，保留前端 build，并将 Docker smoke 收窄为核心链路 `mysql + backend + frontend`。
- [x] 修复 Docker/prod 启动失败：支付网关 `wechatPayGateway` / `alipayGateway` 注入冲突，验证 `/actuator/health` 返回 `UP`。

## Next

- [x] 修复 `CustomerPortalController.pay()` 事务、流水 rows 检查、JDBC 写后读取风险。
- [x] 审计日志补充 `password` / `token` / `secret` 等字段级脱敏。
- [ ] 修复 `BirthdayConfigController` Entity 返回问题。
- [ ] 评估并处理 `GroupBuyExpiryScheduler` 多实例锁。
- [ ] 按 `docs/20` 验收预约日历看板，补齐移动端、空态、未分配预约展示等缺口。

## Later

- [ ] Redis Phase 9：分布式锁、限流、缓存、支付幂等。
- [ ] 真实微信/支付宝 SDK 与阿里云 SMS 接入。
- [ ] 试点后再启动 AI 流失预测 MVP。
