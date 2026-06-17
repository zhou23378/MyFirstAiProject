# 🚀 项目启动引导

> **换电脑/新会话后，对 Agent 说："读取 \_startup.md ，了解项目状态。我已经从旧电脑导出了 salon_sync.sql，salon_data_only.sql。"**

---

## 一、项目概况

| 项目     | 美发沙龙会员管理系统 (Salon Manager)                     |
| -------- | -------------------------------------------------------- |
| 后端     | Spring Boot 3.2.0 + MyBatis Plus 3.5.5 + JDK 21          |
| 前端     | Vue 3 (Composition API) + Vite 5 + Element Plus + Pinia  |
| 数据库   | MySQL 8.x (database: `salon`)                            |
| 后端端口 | 8080                                                     |
| 前端端口 | 3000                                                     |
| 默认账号 | admin / admin123（密码已 BCrypt 哈希存储，明文不再生效） |

---

## 二、当前开发阶段（2026-05-19 晚间更新）

### 审计与修复状态

- **2026-05-17 首次审计：** 35 项问题，22/22 代码问题已修复 ✅
- **📋 完整审计报告：** `docs/12-商用代码审计未修复项-2026-05-22.md`（281项修复 + 36项深度审计 + 生产级架构评审）

### 演进路线进度

按 `docs/11-需求规划-客满满参考.md` 中的对标分析，当前进度约 65%（76 项功能逐项对标）：

| Phase       | 内容                        | 状态                       |
| ----------- | --------------------------- | -------------------------- |
| Phase 5     | Docker + DevOps             | ✅ 已完成                  |
| Phase 6     | 前端响应式架构              | ✅ 已完成                  |
| **Phase 7** | **店务管理 + 营销增强**     | **✅ 已完成 (2026-05-19)** |
| **Phase 8** | **顾客端 H5 + 支付 + 通知** | **✅ 已完成 (2026-05-19)** |
| Phase 9     | AI 应用层                   | 🔜 下一步                  |
| Phase 10    | 测试 + 优化 + 缓冲          | 📅 待开始                  |

### Phase 7 已交付（店务管理 + 营销增强）

| 模块       | 关键文件                                                      | 说明                                      |
| ---------- | ------------------------------------------------------------- | ----------------------------------------- |
| 技师看板   | `technician/` 包 + `views/technician/index.vue`               | 实时状态卡片 + 倒计时，10s 轮询           |
| 轮牌排队   | `queue/` 包 + `views/queue/index.vue`                         | FIFO 入队/分配/跳过/取消，分配自动变 BUSY |
| 服务计时器 | `timer/` 包 + `components/ServiceTimer.vue`                   | 开始/暂停/继续/完成 + 80%/100% 提醒       |
| 财务日结   | `dailyclose/` 包 + `views/dailyClose/index.vue`               | 系统汇总 vs 人工录入对账 + 锁定           |
| 生日营销   | `marketing/scheduler/BirthdayScheduler.java`                  | 定时 8:00 自动发券 + 短信                 |
| RFM 分层   | `marketing/scheduler/RfmScheduler.java`                       | 周度 8 分层计算，仪表盘饼图               |
| 自动标签   | `tag_rule` + `member_tag` 表 + `views/marketing/tagRules.vue` | 条件规则引擎 + 定时打标                   |

### Phase 8 已交付（顾客端 H5 + 支付 + 通知）

| 模块     | 关键文件                                                        | 说明                                                    |
| -------- | --------------------------------------------------------------- | ------------------------------------------------------- |
| 顾客认证 | `customer/` 包（entity/mapper/service/controller/dto）          | 手机号+验证码登录，独立 UUID Token，X-Customer-Token 头 |
| 在线预约 | `CustomerBookingController.java` + `views/customer/booking.vue` | 选服务→选时间→确认 三步向导，时段冲突检测               |
| 顾客门户 | `CustomerPortalController.java` + `views/customer/profile.vue`  | 个人资料/消费记录/优惠券查询                            |
| 支付模块 | `payment/` 包（gateway 接口 + mock 实现 + controller）          | 微信/支付宝 Mock 网关，回调接口，待商户审核后切真实 SDK |
| 前端 H5  | 5 个顾客页面 + `CustomerLayout.vue`                             | 移动端底部标签导航，独立路由 `/h5/*`，独立 auth guard   |
| 安全配置 | `SecurityConfig.java` + `RateLimitFilter.java`                  | 顾客 auth + 支付回调白名单，send-code 限流 1次/分钟     |

### 待启动（按优先级）

1. **Phase 9：AI 应用层** — 流失预测（XGBoost）+ 服务推荐（协同过滤）+ 智能报表（Text-to-SQL）
2. **Phase 10：测试 + 优化 + 缓冲** — 集成测试 + 性能审计 + Docker Demo
3. **微信小程序** — 延后项目，H5 先验证体验
4. **多门店管理** — 延后，单店商用后

---

## 三、文档导航

**项目共 18 份文档。详细按任务查阅指南见 [CLAUDE.md §6](CLAUDE.md)——本节仅列新会话最低阅读量。**

### 3.1 首次会话最低必读（15 分钟）

| # | 文档 | 用时 | 内容 |
|---|------|------|------|
| 1 | [01-代码生成策略.md](docs/01-代码生成策略.md) | 5 min | 商用编码标准 §四~§七 + frontend-first 流程 |
| 2 | [02-前端样式策略.md](docs/02-前端样式策略.md) | 2 min | CSS 4 层架构 + scoped + CSS 变量 |
| 3 | [03-新功能开发背景准备文档.md](docs/03-新功能开发背景准备文档.md) | 3 min | 技术栈、24 个后端模块、目录全景 |
| 4 | [12-商用代码审计未修复项-2026-05-22.md](docs/12-商用代码审计未修复项-2026-05-22.md) | 5 min | 281项修复 + 架构评审，了解质量基线和生产差距 |

### 3.2 文档分类速查

```
开发规范:  01-代码生成策略    02-前端样式策略    03-新功能开发背景准备文档
接口/数据:  04-API接口详细文档  05-数据库参考      06-核心业务流程
业务/规划:  08-需求文档        11-需求规划         10-开发策略         09-业务闭环
基础设施:  15-部署与运维手册
质量/审计:  13-已知问题         14-功能测试目录    12-审计报告+架构评审
变更/记录:  16-数据库变更策略  07-前端路由清单
故障排查:  18-API修复策略
AI 专项:   17-AI集成技术方案
```

---

## 四、开发铁律

### 商用编码标准（强制）

**生成任何代码前，先读 `docs/01-代码生成策略.md` §四~§七。** 该文档包含：

- **§四 商用级代码质量标准** — 安全/数据/错误处理/工程化/前端/SQL 全量规则表
- **§六 审计反模式** — 本项目真实踩过的 8 个坑，附错误/正确代码对比
- **§七 代码审查自检清单** — 提交前逐项自检

以下是最容易违反的关键规则摘要：

| 类别 | 规则                                         | 禁止                    |
| ---- | -------------------------------------------- | ----------------------- |
| 安全 | 密码/密钥/Token 全用环境变量                 | 硬编码任何敏感值        |
| 安全 | SQL 参数化查询                               | 字符串拼接 SQL          |
| 数据 | 计数器/库存用原子 `UPDATE SET qty = qty + ?` | 读-改-写（竞态条件）    |
| 数据 | 金额用 `BigDecimal.setScale(2, HALF_UP)`     | `intValue()` 截断       |
| 错误 | 后端 try-catch + 前端 try-catch              | 异常直抛到用户          |
| 工程 | 批量 `batchUpdate`                           | for 循环单条 INSERT     |
| 工程 | `@Transactional` 包裹多表写入                | 无事务裸写              |
| 前端 | `try-catch` + `ElMessage.error`              | `.then()` 无 `.catch()` |

### 开发门禁（硬规则）

```
编码前必须完成 4 步：
  STEP 1: 读取 docs/01 → docs/02 → docs/03 → docs/04 → docs/05 → docs/06
  STEP 2: 列出所有新增/变更的 API 路径 + 请求/响应结构
  STEP 3: 列出所有要修改的文件（后端 + 前端 + 配置）
  STEP 4: 向用户展示方案，等待批准后再写代码
```

### 关键规范

- **API 路径必须以 `/api` 开头**（前端 request.js 无 baseURL，Vite 代理 /api → 8080）
- **前端接收的是解包后的 data 字段**（axios 拦截器自动解包 `{code, msg, data}`）
- **敏感配置用环境变量，禁止硬编码**
- **Entity 不能直接暴露给 API**，请求用 DTO + `@Valid`，响应用 VO
- **前端样式必须用 scoped + CSS 变量**，颜色禁止硬编码
- **修改完代码后必须验证**：`mvn compile` + `npx vite build` + 启动后端 curl 测试

### 已知坑位

| 坑                                       | 表现                    | 解决                                                             |
| ---------------------------------------- | ----------------------- | ---------------------------------------------------------------- |
| 后端进程是旧代码                         | API 返回 500 但代码已改 | `taskkill //PID xxx //F` 杀旧进程，重新 `mvn compile` + 启动     |
| 前端 API 路径写错                        | 404                     | 检查必须以 `/api` 开头，对比后端 Controller 的 `@RequestMapping` |
| DB role 是大写 `ADMIN`                   | 前端菜单不显示          | auth.js 已做 `.toLowerCase()` 归一化，登录后重试                 |
| service_card / shift_template 等表不存在 | API 500                 | 运行 schema.sql 中的 Phase 4 迁移（存储过程已包含幂等逻辑）      |

---

## 五、环境要求与启动

### 5.1 必需环境

| 组件    | 要求               | 检查命令                                             | 说明                                                  |
| ------- | ------------------ | ---------------------------------------------------- | ----------------------------------------------------- |
| JDK     | 21+（系统 JDK）    | `java -version`                                       | 使用系统安装的 JDK 21，不依赖项目自带 JDK              |
| Maven   | 3.9+               | `mvn -version`                                       |                                                       |
| Node.js | 18+                | `node -v`                                            |                                                       |
| MySQL   | 8.x 运行中         | `netstat -ano \| grep :3306`                         |                                                       |
| Python  | 3.9+               | `python --version`                                   | AI 功能需要（Phase 5+）                               |

### 5.2 配置分层体系（2026-05-18 更新）

**application.yml → 仅声明通用配置，敏感值已掏空。**

| 文件                   | 用途     | 包含内容                                               |
| ---------------------- | -------- | ------------------------------------------------------ |
| `application.yml`      | 主配置   | 端口、MyBatis、日志、profile=**dev**（默认）           |
| `application-dev.yml`  | 开发环境 | 真实 DB 连接、JWT 默认密钥、Swagger 开启               |
| `application-prod.yml` | 生产环境 | DB 连接 + JWT 密钥 全部来自 **环境变量**，Swagger 关闭 |

> **新电脑启动时默认使用 dev profile，无需额外配置。**

### 5.3 数据库初始化（换电脑必看）

**schema.sql 已包含所有迁移（幂等），可重复执行：**

```sql
CREATE DATABASE IF NOT EXISTS salon DEFAULT CHARACTER SET utf8mb4;
-- 然后执行 backend/src/main/resources/schema.sql
```

schema.sql 使用 `DELIMITER // CREATE PROCEDURE ... END // CALL ... DROP PROCEDURE` 模式实现幂等迁移，可重复执行。

**安全加固新增的数据库变更（schema.sql 已含）：**

| 变更项                   | 说明                                                                               |
| ------------------------ | ---------------------------------------------------------------------------------- |
| `audit_log` 表（新建）   | 审计日志表，记录所有敏感操作（登录/创建/修改/删除）                                |
| `admin.role` 列（ALTER） | VARCHAR(20) DEFAULT 'ADMIN'，支持 ROLE_ADMIN / ROLE_SYSTEM 角色区分                |
| admin 密码迁移           | **BCrypt 哈希存储**，明文 `admin123` 已不可直接使用；schema.sql 已插入 BCrypt 密文 |

### 5.4 启动后端

```bash
# 1. 确认系统 JDK 21+ 可用
java -version
# 应显示: openjdk version "21.x.x" ...

# 2. 编译 + 启动（默认 dev profile）
cd backend
mvn compile -q
mvn spring-boot:run -q &
# 验证: curl -s -X POST http://localhost:8080/api/auth/login \
#   -H "Content-Type: application/json" \
#   -d '{"username":"admin","password":"admin123"}'
```

### 5.5 启动前端

```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:3000
```

### 5.6 Python AI 环境（AI 功能需要）

```bash
# 1. 安装 Python 依赖（只需首次执行）
pip install -r requirements_ai.txt

# 2. 启动 FastAPI AI 服务（必须先启，模型自动下载到 ~/.cache/）
cd ai
uvicorn server:app --host 127.0.0.1 --port 8000

# Docker 部署提示：若 AI 服务运行在容器内，需设置 AI_HOST=0.0.0.0
# 使容器端口可被宿主机访问。docker-compose.yml 中 CMD 已包含 --host 0.0.0.0

# 3. 首次或数据更新后，重建向量库
python embed_from_db.py

# 4. 验证
curl "http://127.0.0.1:8000/api/ai/search?q=高消费会员&top_k=5"
curl "http://127.0.0.1:8000/api/ai/health"
```

> **chroma_db/** 目录是向量数据库持久化目录。换电脑后如果没有拷贝此目录，需重新执行步骤 3。

### 5.7 验证端点

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 关键端点测试
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/member/1
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/consumption/member/1/page?page=1&pageSize=10"
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/service-item
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/member-level/list
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/service-card/page?memberId=1&pageSize=100"
```

---

## 六、当前任务进展

### 6.1 Phase 5 Sprint 1 安全加固 — ✅ 已完成（2026-05-18）

**背景：** 2026-05-17 全项目扫描审计发现 10 个严重安全问题（明文密码、硬编码密钥、无 Spring Security 等）。

**成果：7 天全部完成，前后端编译通过。**

| 天   | 任务                                  | 状态 |
| ---- | ------------------------------------- | ---- |
| D1-2 | Spring Security + BCrypt + JWT 过滤器 | ✅   |
| D3   | 环境变量化敏感配置                    | ✅   |
| D4   | 登录限流 + CORS + 关闭生产 Swagger    | ✅   |
| D5   | 审计日志 AOP                          | ✅   |
| D6   | 前端安全适配 + 路由守卫角色校验       | ✅   |
| D7   | 全量回归测试（前后端编译通过）        | ✅   |

<details>
<summary><b>完整文件变更清单（点击展开）</b></summary>

**新建文件（8 个）：**
| 文件 | 说明 |
|------|------|
| `backend/.../config/SecurityConfig.java` | Spring Security 主配置（14 个过滤器链、BCrypt bean、端点白名单） |
| `backend/.../security/JwtAuthenticationFilter.java` | JWT 认证过滤器（替代旧 JwtInterceptor） |
| `backend/.../security/RateLimitFilter.java` | 登录限流过滤器（60秒最多5次） |
| `backend/.../security/AdminUserDetailsService.java` | Spring Security UserDetailsService，加载 admin 用户+role |
| `backend/.../audit/entity/AuditLogEntry.java` | 审计日志实体 |
| `backend/.../audit/mapper/AuditLogMapper.java` | 审计日志 Mapper |
| `backend/.../audit/aspect/AuditLogAspect.java` | 审计日志 AOP 切面 |
| `backend/src/main/resources/application-prod.yml` | 生产环境配置（全部环境变量化） |

**删除文件（1 个）：**
| 文件 | 原因 |
|------|------|
| `backend/.../common/interceptor/JwtInterceptor.java` | 被 JwtAuthenticationFilter 替代 |

**修改文件（14 个）：**
| 文件 | 变更内容 |
|------|---------|
| `backend/pom.xml` | 添加 spring-boot-starter-security |
| `backend/.../common/util/JwtUtil.java` | generateToken() 签名加 role 参数 |
| `backend/.../auth/service/impl/AdminServiceImpl.java` | 密码校验改用 BCryptPasswordEncoder.matches() |
| `backend/.../auth/controller/AdminController.java` | 创建时 BCrypt 加密 + @Valid + DTO |
| `backend/.../config/WebMvcConfig.java` | 删除旧 JwtInterceptor 注册（注：该文件已在 Sprint 3 清理中整体删除） |
| `backend/.../common/exception/GlobalExceptionHandler.java` | 新增 AccessDeniedException 全局拦截 |
| `backend/src/main/resources/application.yml` | 敏感值移除，profile=dev |
| `backend/src/main/resources/application-dev.yml` | 新增开发环境真实配置 |
| `schema.sql` | 新增 audit_log 表 + admin.role 列 + BCrypt 密码 |
| `frontend/src/views/Login.vue` | 移除硬编码默认凭证和密码提示 |
| `frontend/src/router/index.js` | 路由守卫添加角色权限校验 |
| `frontend/src/api/request.js` | 401 清除全面化（localStorage + sessionStorage + store） |
| `frontend/.env.production` | 新增生产环境 API 基地址 |
| `.gitignore` | 新增环境变量文件排除规则 |

</details>

**安全成果摘要：**

- Spring Security 过滤器链完整接入（认证+授权）
- 密码 BCrypt(10) 哈希，明文已从全栈移除
- JWT HS384 签名，密钥分环境管理（dev 默认 / prod 环境变量）
- 登录限流 60秒5次
- CORS 精细化配置（dev localhost:3000 / prod 环境变量）
- Swagger 生产环境关闭
- 审计日志 AOP（@AuditLog 注解驱动，记录登录/创建/修改/删除）
- 前端路由守卫按角色校验（ROLE_ADMIN / ROLE_SYSTEM）

---

### 6.2 AI 会员语义搜索 — ✅ 工程化完成（2026-05-18 晚间）

**架构：** FastAPI (Python) ↔ Spring Boot (RestClient) ↔ Vue 3 (搜索框)

**技术栈：** BAAI/bge-small-zh-v1.5（24MB 中文语义向量模型，512 维）+ ChromaDB（持久化向量库）

**数据管道（MySQL → ChromaDB）：**

| 文件                   | 说明                                                                                                   |
| ---------------------- | ------------------------------------------------------------------------------------------------------ |
| `ai/embed_from_db.py`  | **正式数据管道**：连接 MySQL → 读取 member 表 + 消费统计 → 生成自然语言描述 → BGE 嵌入 → 存入 ChromaDB |
| `ai/server.py`         | **FastAPI 推理服务**：`GET /api/ai/search?q=...&top_k=5` + `/api/ai/health`                            |

**三层对接：**

| 层     | 文件                  | 关键点                                                           |
| ------ | --------------------- | ---------------------------------------------------------------- |
| Python | `ai/server.py`        | SearchResult 含 `member_id`、`name`、`description`、`similarity` |
| Java   | `AiSearchResult.java` | record 含 `int member_id`，接收 Python 返回结果                  |
| Java   | `AiController.java`   | `GET /api/ai/search` → RestClient → FastAPI → 返回给前端         |
| Vue    | `member/index.vue`    | AI 搜索框 + 结果卡片（相似度标签 + 点击跳转 `/member/{id}`）     |

**测试数据：** 32 个测试会员及 113 条消费订单已包含在 `db/salon_data_only.sql` 中。

**已验证的语义搜索查询：**

| 查询               | Top-1 结果                        | 相似度 |
| ------------------ | --------------------------------- | ------ |
| 高消费贵宾         | 王建国（钻石会员，消费 3310 元）  | 59.4%  |
| 快流失的客户       | 林美琪（前活跃客户，2 个月没来）  | 55.7%  |
| 喜欢染发的潮流青年 | 周明伟（学生党，染发潮人）        | 57.8%  |
| 脱发焦虑           | 曹阳（程序员，防脱需求）          | 60.2%  |
| 能帮忙推荐新客的人 | 冯阿姨（社区红人，带来 10+ 客户） | 55.8%  |
| 刚办卡的新会员     | 丁小雨（新会员，1 次消费）        | 71.3%  |

**⚠️ 语义搜索限制：** 不理解数值比较。"余额大于 2000"不会过滤余额，而是把所有提到"余额"的会员都返回。数值筛选应使用会员列表的高级筛选功能。

**启动方式：**

```bash
# 1. Python AI 服务（必须先启）
cd ai
pip install -r ../requirements_ai.txt
uvicorn server:app --host 127.0.0.1 --port 8000

# Docker 部署提示：若 AI 服务运行在容器内，需设置 AI_HOST=0.0.0.0
# 使容器端口可被宿主机访问。docker-compose.yml 中 CMD 已包含 --host 0.0.0.0

# 2. 首次或数据更新后重建向量库
python embed_from_db.py

# 3. 验证
curl "http://127.0.0.1:8000/api/ai/search?q=高消费会员&top_k=5"
curl "http://127.0.0.1:8000/api/ai/health"
```

> **chroma_db/** 是向量数据库持久化目录。换电脑后如果没拷贝此目录，需重新执行 `python embed_from_db.py`。

---

### 6.3 会员高级筛选 — ✅ 已完成（2026-05-18 晚间）

**功能：** 会员列表页新增可展开的「高级筛选」面板，支持 7 种条件自由组合。

**后端 API：** `GET /api/member/page` 新增可选参数（向后兼容）：

| 参数                                  | 类型       | 说明                                     |
| ------------------------------------- | ---------- | ---------------------------------------- |
| `balanceMin` / `balanceMax`           | BigDecimal | 余额范围                                 |
| `level`                               | Integer    | 等级 ID 精确匹配                         |
| `gender`                              | Integer    | 性别（1=男 2=女 0=未知）                 |
| `totalConsumeMin` / `totalConsumeMax` | BigDecimal | 累计消费范围（子查询 consumption_order） |
| `lastConsumeDays`                     | Integer    | 最后消费 >N 天前（含从未消费的）         |

**改动文件（3 个）：**

| 文件                                              | 变更                                    |
| ------------------------------------------------- | --------------------------------------- |
| `backend/.../dto/MemberQueryDTO.java`             | 新增 8 个筛选字段                       |
| `backend/.../service/impl/MemberServiceImpl.java` | `pageQuery()` 添加 7 种 WHERE 条件      |
| `frontend/src/views/member/index.vue`             | 搜索栏增加「高级筛选」按钮 + 可展开面板 |

**Bug 修复：** 累计消费子查询 `WHERE member_id = id` 在 MySQL 中 `id` 被解析为 `consumption_order.id`（内部表），修正为 `WHERE member_id = member.id`。

**验证结果：**

| 筛选条件                    | 结果                |
| --------------------------- | ------------------- |
| `balanceMin=2000`           | 7 条                |
| `level=4 & balanceMin=5000` | 3 条（钻石+高余额） |
| `lastConsumeDays=30`        | 5 条（流失风险）    |
| `totalConsumeMin=1000`      | 15 条（高消费）     |

### 6.4 Phase 7 店务管理 + 营销增强 — ✅ 已完成（2026-05-19）

**新增数据库表（7 张）：**

| 表名                | 说明         | 关键字段                                                                             |
| ------------------- | ------------ | ------------------------------------------------------------------------------------ |
| `technician_status` | 技师实时状态 | employee_id, status(AVAILABLE/BUSY/BREAK/OFF_DUTY), current_customer_name            |
| `service_queue`     | 轮牌排队     | member_id, queue_number, status(1=等待/2=已分配/3=取消/4=跳过), assigned_employee_id |
| `service_timer`     | 服务计时     | appointment_id, employee_id, planned_duration, status(1=进行中/2=暂停/3=完成)        |
| `daily_close`       | 财务日结     | close_date, system_cash/wechat/alipay, manual_cash/wechat/alipay, diff_amount        |
| `notification_log`  | 通知日志     | member_id, phone, type, content, status(0=发送中/1=成功/2=失败)                      |
| `tag_rule`          | 标签规则     | name, tag_name, conditions_json, enabled                                             |
| `member_tag`        | 会员标签     | member_id, tag_name, source(AUTO/MANUAL), rule_id                                    |

**member 表新增字段（5 个）：**
`rfm_segment`, `last_visit_date`, `total_spent`, `visit_count`, `birthday`

**新建后端模块（4 个包）：**

- `com.salon.technician` — entity/mapper/service/controller/dto
- `com.salon.queue` — entity/mapper/service/controller/dto
- `com.salon.timer` — entity/mapper/service/controller
- `com.salon.dailyclose` — entity/mapper/service/controller
- `com.salon.notification` — entity/mapper/service（SmsService 接口 + MockSmsServiceImpl）
- `com.salon.marketing/scheduler` — BirthdayScheduler, RfmScheduler

**新增前端页面（4 个）：**

- `views/technician/index.vue` — 技师看板
- `views/queue/index.vue` — 轮牌排队
- `views/dailyClose/index.vue` — 财务日结
- `views/marketing/tagRules.vue` — 标签规则

**新增前端组件（5 个）：**

- `MobileTabBar.vue`, `ResponsiveDataList.vue`, `DrawerFilter.vue`, `PageHeader.vue`, `ServiceTimer.vue`

**菜单路由新增：**

- `/technician-status` (Monitor), `/service-queue` (List), `/daily-close` (DataAnalysis), `/marketing/tag-rules` (Setting)

---

### 6.5 Phase 8 顾客端 H5 + 支付 — ✅ 已完成（2026-05-19 晚间）

**新增数据库表（1 张） + member 表变更：**

| 变更                          | 说明                                                                       |
| ----------------------------- | -------------------------------------------------------------------------- |
| `customer_session` 表（新建） | id, member_id, token(UNIQUE), login_method, expire_at                      |
| `payment_detail` 表（新建）   | id, order_id, pay_method, amount, transaction_id, pay_channel, qr_code_url |
| `member.password` 列（ALTER） | VARCHAR(255), BCrypt 加密，可空                                            |
| `member.openid` 列（ALTER）   | VARCHAR(64), 微信 OpenID                                                   |

**新建后端模块（2 个包）：**

| 包                              | 文件                                                        | 说明                                                             |
| ------------------------------- | ----------------------------------------------------------- | ---------------------------------------------------------------- |
| `com.salon.customer`            | entity/mapper/service/impl/controller + 3 DTO               | 顾客认证 + 会话管理                                              |
| `com.salon.customer.controller` | `CustomerBookingController.java`                            | 6 端点：services/technicians/slots/submit/my-appointments/cancel |
| `com.salon.customer.controller` | `CustomerPortalController.java`                             | 6 端点：profile/orders/coupons CRUD                              |
| `com.salon.payment`             | gateway 接口 + MockWechatPayGateway + MockAlipayGateway     | 支付网关（Mock 模式，待商户号审批后切真实 SDK）                  |
| `com.salon.payment.controller`  | `PaymentController.java` + `PaymentCallbackController.java` | 创建支付/状态查询/微信回调/支付宝回调                            |

**认证架构要点：**

- 顾客认证独立于管理端 JWT，使用 **UUID Token** 存储在 `customer_session` 表
- 前端通过 `X-Customer-Token` 请求头传递（非 Authorization: Bearer）
- 顾客端路由 `/h5/*`，独立路由守卫检查 `localStorage.customer_token`
- SecurityConfig 白名单：`/api/customer/**`（所有顾客端点，Controller 自行校验 X-Customer-Token）和 `/api/payment/callback/**`（支付回调）

**新建前端文件（9 个）：**

| 文件                         | 说明                                           |
| ---------------------------- | ---------------------------------------------- |
| `api/customerRequest.js`     | 顾客专用 axios 实例（X-Customer-Token 拦截器） |
| `api/customer.js`            | 顾客 API 层（sendCode/login/getMe/logout）     |
| `store/customerAuth.js`      | 顾客 Pinia store（token/memberId/name/phone）  |
| `layouts/CustomerLayout.vue` | 顾客端布局（顶部标题栏 + 底部标签栏 + 内容区） |
| `views/customer/login.vue`   | 手机号 + 验证码登录页                          |
| `views/customer/index.vue`   | 顾客首页（积分/余额/优惠券卡片 + 快捷入口）    |
| `views/customer/booking.vue` | 3 步预约向导（选服务→选时间→确认）             |
| `views/customer/payment.vue` | 支付页（支付方式选择 + QR 码展示）             |
| `views/customer/profile.vue` | 个人中心（资料/消费记录/优惠券/次卡）          |

**路由变更：** `router/index.js` 新增 `/h5/*` 路由组，使用 `CustomerLayout`，独立 auth guard。

**安全配置变更：**

- `SecurityConfig.java`：白名单新增 `/api/customer/**` + `/api/payment/callback/**`
- `RateLimitFilter.java`：重构支持多端点限流，`/api/customer/auth/send-code` 限流 1次/分钟/IP

### 6.7 本地验证与 Bug 修复 — ✅ 已完成（2026-05-19 深夜）

**AI Python 服务验证：**

- FastAPI 启动 ✅，BGE 模型加载正常
- `/api/ai/health` → `{"status":"ok","vectors":32}`
- 语义搜索 "高消费" → 王建国 61.3%、苏老板 60.2%
- Java BFF `/api/ai/search` 全链路通过 ✅

**顾客认证全链路验证：**

- `POST /send-code` → 200 ✅
- `POST /login` → 200，返回 token + memberId + name + phone ✅
- `GET /me` → 200，password 已置空 ✅
- 第二次 send-code → 429 限流生效 ✅
- 未注册手机号 → 400 "未注册" ✅

**预约 & 门户验证：**

- `GET /booking/services` → 200，返回 6 个服务 ✅
- `GET /portal/orders?page=1&size=3` → 200，分页正常 ✅

**Bug 修复（2 个）：**

| Bug                             | 根因                                                                       | 修复                               |
| ------------------------------- | -------------------------------------------------------------------------- | ---------------------------------- |
| send-code 500 错误              | `notification_log` 表缺 `update_time` 列，但实体继承了 BaseEntity          | ALTER TABLE 加列 + schema.sql 修正 |
| booking/portal 返回 401         | SecurityConfig 白名单只覆盖 `/api/customer/auth/**`，未覆盖 booking/portal | 扩大为 `/api/customer/**`          |
| `member_tag` 表缺 `update_time` | 同上，实体继承 BaseEntity                                                  | ALTER TABLE 加列                   |

---

### 6.6 下一任务（按优先级）

1. **Phase 9：AI 应用层**（Day 51-60）
   - 流失预测：XGBoost 模型，特征工程（R/F/M/间隔），周度训练 + 推理 API
   - 服务推荐：协同过滤（会员-服务矩阵 + 余弦相似度），冷启动热度兜底
   - 智能报表：Text-to-SQL（本地 Qwen 2.5 7B），SQL 安全校验（只读，5s 超时）
2. **Phase 10：测试 + 优化 + 缓冲**（Day 61-65）
   - 核心流程集成测试（10-15 个）
   - 性能审计（EXPLAIN + 索引 + N+1）
   - Docker Demo 一键启动
3. **微信小程序** — 延后独立项目
4. **多门店管理** — 延后

---

## 七、项目目录速查

```
MyFirstAiProject/
├── _startup.md                    # ← 当前文件
├── CLAUDE.md                      # 开发规范
├── docs/                          # 18 份项目文档
│   ├── 01-代码生成策略.md
│   ├── 02-前端样式策略.md
│   ├── 03-新功能开发背景准备文档.md
│   ├── 04-API接口详细文档.md
│   ├── 13-已知问题与技术债务.md
│   ├── 11-需求规划-客满满参考.md   # 76项功能逐项对标 + 65%加权进度 + 差距优先级矩阵
│   └── 10-开发策略与优先级.md      # 审计结论 + Sprint 1 计划
├── JDK/                         # 项目自带 JDK 21（禁止使用系统 JDK）
├── ai/                          # AI Python 脚本
│   ├── server.py                # FastAPI 推理服务（搜索 + 健康检查）
│   └── embed_from_db.py         # MySQL → ChromaDB 数据管道（正式）
├── db/                          # 数据库备份
│   ├── salon_sync.sql           # 全量结构+数据导出
│   └── salon_data_only.sql      # 仅数据导出（含 32 个测试会员 + 113 条订单）
├── chroma_db/                   # Chroma 向量数据库持久化目录（换电脑需重生成）
├── requirements_ai.txt          # AI Python 依赖清单
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/salon/
│       │   ├── common/            # BaseEntity, Result, JwtUtil, 异常, @AuditLog 注解
│       │   ├── config/            # MyBatisPlusConfig, WebMvcConfig, SecurityConfig, MyMetaObjectHandler
│       │   ├── security/          # ⭐ 安全模块（JwtAuthenticationFilter, RateLimitFilter, AdminUserDetailsService）
│       │   ├── audit/             # ⭐ 审计日志模块（entity, mapper, aspect AOP）
│       │   ├── auth/              # 认证模块（Admin CRUD, Login）
│       │   ├── member/            # 会员模块（CRUD, 充值, 流失预警, 导入导出, 高级筛选）
│       │   ├── service/           # 服务项目模块（分类 + 项目 CRUD）
│       │   ├── consumption/       # 消费模块（订单, 次卡, 退款）
│       │   ├── employee/          # 员工模块
│       │   ├── memberlevel/       # 会员等级模块
│       │   ├── appointment/       # 预约模块（冲突检测, 转消费）
│       │   ├── inventory/         # 库存模块（商品, 供应商, 出入库）
│       │   ├── schedule/          # 排班考勤模块（班次模板, 排班, 打卡）
│       │   ├── coupon/            # 优惠券模块（模板, 发放, 核销）
│       │   ├── report/            # 报表模块（6 个报表接口）
│       │   ├── dashboard/         # 仪表盘模块
│       │   ├── technician/        # ⭐ Phase 7: 技师状态看板
│       │   ├── queue/             # ⭐ Phase 7: 轮牌排队
│       │   ├── timer/             # ⭐ Phase 7: 服务计时器
│       │   ├── dailyclose/        # ⭐ Phase 7: 财务日结
│       │   ├── notification/      # ⭐ Phase 7: 短信通知（SmsService + Mock）
│       │   ├── marketing/         # ⭐ Phase 7: 营销调度（生日/RFM）
│       │   ├── tag/                # ⭐ Phase 7: 标签规则（条件引擎 + 自动打标）
│       │   ├── customer/          # ⭐ Phase 8: 顾客认证+预约+门户
│       │   └── payment/           # ⭐ Phase 8: 支付网关（Mock 模式）
│       └── resources/
│           ├── application.yml    # 主配置（profile=dev）
│           ├── application-dev.yml  # 开发环境真实配置
│           ├── application-prod.yml # 生产环境（环境变量化）
│           └── schema.sql         # 数据库初始化（含 audit_log + admin.role + BCrypt）
└── frontend/
    ├── .env.production            # ⭐ 生产环境 API 基地址
    └── src/
        ├── api/                   # 18 个 API 模块（含 customerRequest.js + customer.js）
        ├── views/
        │   ├── customer/          # ⭐ Phase 8: login, index, booking, payment, profile（5 H5 页）
        │   ├── technician/        # ⭐ Phase 7: 技师看板
        │   ├── queue/             # ⭐ Phase 7: 轮牌排队
        │   ├── dailyClose/        # ⭐ Phase 7: 财务日结
        │   └── marketing/         # ⭐ Phase 7: 标签规则
        ├── layouts/               # ⭐ Phase 8: CustomerLayout.vue（顾客端布局）
        ├── components/            # AppLayout, MobileTabBar, ResponsiveDataList, DrawerFilter, PageHeader, ServiceTimer 等
        ├── store/
        │   ├── auth.js            # Pinia 管理端认证
        │   └── customerAuth.js    # ⭐ Phase 8: 顾客端认证
        ├── router/index.js        # 路由定义 + 守卫（管理端角色校验 + 顾客端 Token 校验）
        └── styles/                # variables.css, reset.css, element-overrides.css
```

---

## 八、快速状态获取

让 Agent 执行以下命令即可了解最新状态：

```bash
# 数据库表数量（当前约 30 张表）
mysql -uroot -p123456 salon -e "SHOW TABLES;" | wc -l

# 后端编译状态
cd backend && mvn compile -q && echo "BUILD OK" || echo "BUILD FAILED"

# 前端编译状态
cd frontend && npx vite build 2>&1 | tail -3

# 后端是否运行
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/auth/login -X POST \
  -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'

# 顾客端测试（需先获取 customer token）
curl -s -X POST http://localhost:8080/api/customer/auth/send-code \
  -H "Content-Type: application/json" -d '{"phone":"13800138000"}'

curl -s -X POST http://localhost:8080/api/customer/auth/login \
  -H "Content-Type: application/json" -d '{"phone":"13800138000","code":"123456"}'
```

---

## 九、换电脑迁移指南

### 9.1 迁移原理

Docker 将应用和依赖打包在镜像中，但**数据**（MySQL 数据库、ChromaDB 向量库）存储在 Docker Volume 和本地目录中。迁移 = 代码 + 数据。

| 需要迁移      | 方式                    | 说明                                                 |
| ------------- | ----------------------- | ---------------------------------------------------- |
| 项目代码      | 直接复制文件夹          | 含 Dockerfiles、docker-compose.yml、.env、schema.sql |
| MySQL 数据    | `mysqldump` 导出 `.sql` | Docker Volume 不能直接跨机器复制                     |
| ChromaDB 向量 | `chroma_db/` 目录       | 本地目录，直接复制（已随项目文件夹）                 |
| AI 模型缓存   | 无需迁移                | Dockerfile.ai 构建时自动下载 BGE 模型                |
| Docker 镜像   | 无需迁移                | 新机器 `docker compose build` 重新构建               |

### 9.2 旧电脑操作（今晚执行，约 3 分钟）

```bash
# 步骤 1：导出 MySQL 全部数据
# 如果 MySQL 在 Docker 中运行：
docker exec salon-mysql mysqldump -uroot -psalon123 --databases salon \
  --routines --triggers --single-transaction > salon_backup_20260519.sql

# 如果 MySQL 是本地裸机运行（当前状态）：
mysqldump -uroot -p123456 --databases salon \
  --routines --triggers --single-transaction > salon_backup_20260519.sql

# 步骤 2：确认导出文件有效
head -5 salon_backup_20260519.sql
# 应看到: -- MySQL dump ... -- Host: ... -- Server version ...
wc -l salon_backup_20260519.sql
# 应有几千行

# 步骤 3：复制整个项目文件夹到移动硬盘/U盘
# 源路径: D:\Desktop\MyFirstAiProject
# 直接复制整个 MyFirstAiProject 文件夹到新电脑
```

### 9.3 新电脑操作（明天，约 10 分钟）

**前置条件：**

- Docker Desktop 已安装并运行（`docker --version` 确认）
- Node.js 18+ 已安装（前端本地开发用，Docker 内已自带）
- 项目文件夹已从旧电脑复制到新电脑
- `salon_backup_20260519.sql` 已随项目一起复制

```bash
# ============ 步骤 1：进入项目目录 ============
cd MyFirstAiProject

# ============ 步骤 2：确认 .env 文件 ============
ls -la .env
# 如果 .env 不存在，从模板创建：
cp .env.example .env

# ============ 步骤 3：构建并启动全部服务 ============
docker compose up -d --build

# 首次构建会：
#   - 下载 MySQL 8.0 镜像 (~300MB)
#   - 下载 Python 3.11-slim (~150MB)
#   - 下载 BGE 中文模型 (~24MB，Dockerfile 内自动)
#   - Maven 构建后端 jar（多阶段构建）
#   - Node 构建前端 dist（多阶段构建）
#   - 自动执行 schema.sql 初始化 30 张表
# 预计 5-15 分钟，视网络和机器性能

# ============ 步骤 4：等待健康检查 ============
docker ps
# 确认 4 个容器状态均为 healthy：
#   salon-mysql    → healthy
#   salon-backend  → healthy
#   salon-frontend → healthy
#   salon-ai       → healthy

# ============ 步骤 5：导入 MySQL 数据 ============
docker exec -i salon-mysql mysql -uroot -psalon123 salon < salon_backup_20260519.sql

# ============ 步骤 6：重建 AI 向量库 ============
docker exec salon-ai python ai/embed_from_db.py
# 输出应显示: ChromaDB ready, 32 vectors loaded

# ============ 步骤 7：验证 ============
# 后端健康
curl http://localhost:8080/actuator/health
# → {"status":"UP"}

# 前端页面（Docker 中前端在 80 端口）
curl -s -o /dev/null -w "%{http_code}" http://localhost/
# → 200

# AI 服务
curl http://localhost:8000/api/ai/health
# → {"status":"ok","vectors":32}

# 管理端登录
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# → 返回 JWT token

# 顾客端 API
curl -s -X POST http://localhost:8080/api/customer/auth/send-code \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800001001"}'

# 浏览器访问
# 管理端:  http://localhost/
# 顾客端:  http://localhost/h5/login
# Swagger: http://localhost:8080/swagger-ui/index.html
```

### 9.4 常见问题

| 问题                     | 原因                    | 解决                                                                     |
| ------------------------ | ----------------------- | ------------------------------------------------------------------------ |
| 端口冲突（80/3306/8080） | 新电脑已有程序占用      | 修改 `.env` 端口映射，如 `MYSQL_PORT=3307`                               |
| `docker compose` 不识别  | Docker 版本差异         | 旧版用 `docker-compose`（有连字符）                                      |
| MySQL 启动失败           | 本地 MySQL 服务冲突     | `net stop MySQL` 或改 `.env` 端口                                        |
| 后端连不上 MySQL         | MySQL 未 healthy        | docker-compose 已配 `depends_on: condition: service_healthy`，会自动等待 |
| AI 搜索无结果            | 向量库未重建            | 执行步骤 6：`docker exec salon-ai python ai/embed_from_db.py`            |
| 前端除了首页其他页面 404 | Nginx 未配 history 模式 | 确认 `nginx.conf` 存在，其中 `try_files $uri /index.html`                |

### 9.5 混合模式（MySQL Docker + 后端/前端/AI 本地）

如果只想 Docker 跑 MySQL，其他本地开发：

```bash
# 只启动 MySQL
docker compose up -d mysql

# 本地启动后端（连 localhost:3306）
cd backend && mvn spring-boot:run

# 本地启动前端（连 localhost:8080）
cd frontend && npm run dev

# 本地启动 AI（连 localhost:3306）
cd ai && uvicorn server:app --host 127.0.0.1 --port 8000
```

> 本地模式下，后端使用 `application-dev.yml`，数据库连 `localhost:3306`。Docker 全量模式使用 `application-prod.yml`，连 `mysql:3306`（容器内 DNS）。
