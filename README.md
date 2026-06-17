# 美发沙龙会员管理系统 (Salon Manager)

全栈会员管理与店务运营系统，覆盖会员生命周期、消费收银、店务管理、营销自动化、顾客自助端。

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.2.0 + MyBatis Plus 3.5.5 + JDK 21 |
| 前端 | Vue 3 (Composition API) + Vite 5 + Element Plus + Pinia |
| 数据库 | MySQL 8.x (database: `salon`) |
| AI | FastAPI + ChromaDB + BGE 中文语义向量模型 |
| 容器化 | Docker Compose (4 容器: MySQL + Backend + Frontend + AI) |
| 安全 | Spring Security + BCrypt + JWT (HS384) + RBAC 四角色 |

## 功能模块

### 会员与消费
- **会员管理** — CRUD + 高级筛选（余额/等级/性别/消费/流失天数）+ 导入导出 + 详情 + 充值
- **会员等级** — 等级 CRUD + 折扣率 + 积分门槛 + 自动升级
- **消费收银** — 三栏收银（会员→服务→购物车）+ 8 种支付方式 + 退款 + 次卡
- **优惠券** — 模板 CRUD + 发券 + 核销 + 过期处理

### 店务运营 (Phase 7)
- **技师看板** — 实时状态卡片 + 倒计时 + 10s 轮询
- **轮牌排队** — FIFO 入队/分配/跳过/取消 + 自动状态联动
- **服务计时** — 开始/暂停/继续/完成 + 80%/100% 提醒
- **财务日结** — 系统汇总 vs 人工录入对账 + 差异追踪 + 锁定归档

### 营销自动化 (Phase 7)
- **RFM 分层** — 周度自动计算 + 8 段分层 + 仪表盘饼图
- **生日营销** — 定时 8:00 自动发券 + 短信通知
- **自动标签** — 条件规则引擎 + 定时打标

### 顾客端 H5 (Phase 8)
- **手机号登录** — 验证码登录 + UUID Token + 独立会话
- **在线预约** — 选服务→选时间→确认 三步向导 + 时段冲突检测
- **个人中心** — 消费记录/优惠券/次卡查询
- **支付** — 微信/支付宝 Mock 网关 + 回调幂等处理

### 其他模块
- **预约管理** — 预约 CRUD + 状态流转 + 转消费
- **库存管理** — 商品/分类/供应商 + 入库/出库 + 库存预警
- **排班考勤** — 班次模板 + 排班周视图 + 打卡
- **营业报表** — 日报/月报/服务排行/收银日报/员工业绩
- **数据大屏** — 全屏实时数据展示
- **AI 语义搜索** — 自然语言搜会员 + BFF 代理 + 前端搜索卡片
- **系统管理** — 管理员 CRUD + RBAC 四角色（ADMIN/MANAGER/TECHNICIAN/CASHIER）

## 项目结构

```
MyFirstAiProject/
├── _startup.md                    # 项目启动引导（新 Agent 入口）
├── CLAUDE.md                      # AI 开发规范与纪律
├── README.md                      # 本文件
├── docs/                          # 24 份项目文档
├── docker-compose.yml             # 一键编排 4 容器
├── Dockerfile.backend             # 后端镜像
├── Dockerfile.frontend            # 前端镜像
├── Dockerfile.ai                  # AI 服务镜像
├── nginx.conf                     # Nginx 反向代理
├── .env / .env.example            # 环境变量
├── requirements_ai.txt            # AI Python 依赖
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/salon/        # 24 个后端模块（见 _startup.md 完整列表）
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           ├── application-prod.yml
│           └── schema.sql         # 30 张表（幂等，可重复执行）
├── frontend/
│   └── src/
│       ├── api/                   # 18 个 API 模块
│       ├── views/                 # 24 个页面（管理端 19 + H5 5）
│       ├── components/            # 9 个共享组件
│       ├── layouts/               # AppLayout + CustomerLayout
│       ├── store/                 # Pinia (auth + customerAuth)
│       ├── router/index.js        # 26 条路由 + 双端守卫
│       └── styles/                # variables → reset → element-overrides
├── ai/                            # Python AI 微服务
│   ├── server.py                  # FastAPI 推理服务
│   └── embed_from_db.py           # MySQL → ChromaDB 数据管道
├── db/                            # 数据库备份
├── chroma_db/                     # ChromaDB 向量持久化
├── scripts/                       # backup-db.sh / restore-db.sh
└── JDK/                           # 项目自带 JDK 21
```

## 快速启动

### 方式一：Docker（推荐）

```bash
cp .env.example .env
# 编辑 .env 修改 JWT_SECRET 和 DB_PASSWORD
docker compose up -d
# 管理端: http://localhost/
# 顾客端: http://localhost/h5/login
# Swagger: http://localhost:8080/swagger-ui/index.html
```

### 方式二：本地开发

```bash
# 1. 初始化数据库
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS salon DEFAULT CHARSET utf8mb4;"
mysql -uroot -p salon < backend/src/main/resources/schema.sql

# 2. 启动后端 (端口 8080)
cd backend
mvn compile -q && mvn spring-boot:run -q

# 3. 启动前端 (端口 3000)
cd frontend
npm install && npm run dev

# 4. AI 服务 (可选, 端口 8000)
cd ai
pip install -r ../requirements_ai.txt
python embed_from_db.py
uvicorn server:app --host 127.0.0.1 --port 8000
```

## 默认账号

| 用途 | 账号 | 密码 |
|------|------|------|
| 管理端登录 | admin | admin123 (BCrypt) |
| MySQL (dev) | root | 123456 |

## 开发指南

新 Agent / 新会话入口：**读取 `_startup.md`**。

编码前必读：`docs/01-代码生成策略.md` → `docs/02-前端样式策略.md` → `docs/03-新功能开发背景准备文档.md`。
