# API 接口详细文档

> 本文档列出了所有后端接口的完整请求/响应示例。开发新功能调用已有接口时，以本文档为准。
> **注意：本文档基于实际代码生成，与之前文档有差异时，以本文档为准。**

---

## 通用说明

### 响应格式

所有接口统一返回 `Result<T>` 结构：

```json
// 成功（有数据）
{ "code": 200, "msg": "操作成功", "data": { ... } }

// 成功（无数据）
{ "code": 200, "msg": "操作成功", "data": null }

// 分页
{ "code": 200, "msg": "操作成功", "data": { "list": [...], "total": 100, "page": 1, "pageSize": 10, "pages": 10 } }

// 业务错误
{ "code": 400, "msg": "具体错误信息", "data": null }

// 未登录
{ "code": 401, "msg": "未登录或Token已过期", "data": null }
```

### 前端数据解包约定

`request.js` 响应拦截器执行 `return res.data.data`，因此前端 `await apiCall()` 拿到的是 **data 内部的对象**，不是完整的 `{ code, msg, data }`：

- 非分页接口 → 前端拿到 `{ ... }`（即 data 的内容）
- 分页接口 → 前端拿到 `{ list, total, page, pageSize, pages }`

### 认证方式

除登录接口外，所有 `/api/**` 请求需携带：

```
Authorization: Bearer <jwt_token>
```

Token 有效期 24 小时。

---

## 1. 认证模块 `/api/auth`

### POST `/api/auth/login` — 登录

**请求体：**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**字段说明：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**成功响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "admin"
  }
}
```

**失败响应：**
```json
// 用户名或密码错误 (401)
{ "code": 401, "msg": "用户名或密码错误", "data": null }

// 参数校验失败 (400)
{ "code": 400, "msg": "不能为空", "data": null }
```

**不需要 Token 认证。**

---

## 2. 会员模块 `/api/member`

### GET `/api/member/page` — 分页查询

**查询参数：**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| name | String | 否 | — | 按姓名模糊搜索 |
| phone | String | 否 | — | 按手机号模糊搜索 |
| page | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |

**请求示例：** `GET /api/member/page?name=张&balanceMin=1000&level=3&page=1&pageSize=10`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 姓名模糊搜索 |
| phone | String | 否 | 手机号模糊搜索 |
| balanceMin | BigDecimal | 否 | 余额下限 |
| balanceMax | BigDecimal | 否 | 余额上限 |
| level | Integer | 否 | 等级ID精确匹配 |
| gender | Integer | 否 | 性别（1=男 2=女 0=未知） |
| totalConsumeMin | BigDecimal | 否 | 累计消费金额下限（子查询） |
| totalConsumeMax | BigDecimal | 否 | 累计消费金额上限（子查询） |
| lastConsumeDays | Integer | 否 | 最后消费距今天数下限（>N 天未消费） |
| page | Integer | 否 | 页码（默认 1） |
| pageSize | Integer | 否 | 每页条数（默认 10） |

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "张三",
        "phone": "13800138001",
        "gender": 1,
        "level": 2,
        "levelName": "银卡会员",
        "points": 150,
        "balance": 200.00,
        "birthday": "1990-05-20",
        "tags": "高消费,常客",
        "rfmSegment": "CHAMPIONS",
        "lastVisitDate": "2026-05-20",
        "totalSpent": 5000.00,
        "visitCount": 25,
        "lastConsumeDate": "2026-05-20",
        "createTime": "2026-05-14 10:30:00",
        "updateTime": "2026-05-14 10:30:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 10,
    "pages": 5
  }
}
```

**前端拿到（拦截器解包后）：**
```json
{
  "list": [
    { "id": 1, "name": "张三", "phone": "13800138001", "gender": 1, "level": 2, "levelName": "银卡会员", "points": 150, "balance": 200.00, "birthday": "1990-05-20", "tags": "高消费,常客", "rfmSegment": "CHAMPIONS", "lastVisitDate": "2026-05-20", "totalSpent": 5000.00, "visitCount": 25, "createTime": "2026-05-14 10:30:00", "updateTime": "2026-05-14 10:30:00" }
  ],
  "total": 50,
  "page": 1,
  "pageSize": 10,
  "pages": 5
}
```

> **字段说明：** 完整 Member 实体包含 18+ 字段。`password` 和 `openid` 字段始终不返回。`tags` 为逗号分隔标签字符串。`rfmSegment` 可选值：CHAMPIONS / LOYAL / POTENTIAL / AT_RISK / LOST。

### GET `/api/member/{id}` — 根据 ID 查询

**路径参数：** `id` — 会员ID（Long）

**请求示例：** `GET /api/member/1`

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "张三",
    "phone": "13800138001",
    "gender": 1,
    "level": 2,
    "levelName": "银卡会员",
    "points": 150,
    "balance": 200.00,
    "birthday": "1990-05-20",
    "tags": "高消费,常客",
    "rfmSegment": "CHAMPIONS",
    "lastVisitDate": "2026-05-20",
    "totalSpent": 5000.00,
    "visitCount": 25,
    "lastConsumeDate": "2026-05-20",
    "createTime": "2026-05-14 10:30:00",
    "updateTime": "2026-05-14 10:30:00"
  }
}
```

### POST `/api/member` — 新增会员

**请求体：**
```json
{
  "name": "张三",
  "phone": "13800138001",
  "gender": 1,
  "level": 1,
  "points": 0,
  "balance": 0.00
}
```

**字段说明：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 姓名，不能为空 |
| phone | String | 否 | 手机号，需匹配 `^1[3-9]\d{9}$` |
| gender | Integer | 否 | 性别：0未知/1男/2女 |
| level | Integer | 否 | 会员等级ID（关联 member_level.id） |
| points | Integer | 否 | 积分 |
| balance | BigDecimal | 否 | 余额 |

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "张三",
    "phone": "13800138001",
    "gender": 1,
    "level": 1,
    "points": 0,
    "balance": 0.00,
    "createTime": "2026-05-14 10:30:00",
    "updateTime": "2026-05-14 10:30:00"
  }
}
```

### PUT `/api/member/{id}` — 修改会员

**路径参数：** `id` — 会员ID

**请求体：** 同新增接口

**响应 (200)：** 返回修改后的完整会员对象，结构同上。

### DELETE `/api/member/{id}` — 删除会员

**路径参数：** `id` — 会员ID

**响应 (200)：**
```json
{ "code": 200, "msg": "操作成功", "data": null }
```

---

## 3. 服务分类模块 `/api/service-category`

> ⚠️ 路径前缀为 `/api/service-category`，**不是** `/api/service/category`

### GET `/api/service-category` — 查询所有分类

**请求示例：** `GET /api/service-category`

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "剪发",
      "sort": 1,
      "createTime": "2026-05-14 10:00:00",
      "updateTime": "2026-05-14 10:00:00"
    },
    {
      "id": 2,
      "name": "烫染",
      "sort": 2,
      "createTime": "2026-05-14 10:00:00",
      "updateTime": "2026-05-14 10:00:00"
    }
  ]
}
```

### POST `/api/service-category` — 新增分类

**请求体：**
```json
{
  "name": "护理",
  "sort": 3
}
```

**响应 (200)：** 返回带 ID 的完整分类对象。

### PUT `/api/service-category/{id}` — 修改分类

**请求体：** 同新增

**响应 (200)：** 返回修改后的分类对象。

### DELETE `/api/service-category/{id}` — 删除分类

**响应 (200)：**
```json
{ "code": 200, "msg": "操作成功", "data": null }
```

---

## 4. 服务项目模块 `/api/service-item`

> ⚠️ 路径前缀为 `/api/service-item`，**不是** `/api/service/item`

### GET `/api/service-item` — 查询所有项目

**请求示例：** `GET /api/service-item`

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "categoryId": 1,
      "name": "洗剪吹",
      "price": 68.00,
      "duration": 30,
      "status": 1,
      "createTime": "2026-05-14 10:00:00",
      "updateTime": "2026-05-14 10:00:00"
    }
  ]
}
```

### GET `/api/service-item/category/{categoryId}` — 按分类查询项目

**路径参数：** `categoryId` — 分类ID

**请求示例：** `GET /api/service-item/category/1`

**响应 (200)：** 结构同上（按分类过滤的列表）。

### POST `/api/service-item` — 新增项目

**请求体：**
```json
{
  "categoryId": 1,
  "name": "洗剪吹",
  "price": 68.00,
  "duration": 30,
  "status": 1
}
```

**字段说明：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Long | 是 | 所属分类ID |
| name | String | 是 | 项目名称 |
| price | BigDecimal | 是 | 价格 |
| duration | Integer | 否 | 时长（分钟） |
| status | Integer | 否 | 状态：1上架/0下架 |

**响应 (200)：** 返回带 ID 的完整项目对象。

### PUT `/api/service-item/{id}` — 修改项目

**请求体：** 同新增

**响应 (200)：** 返回修改后的项目对象。

### DELETE `/api/service-item/{id}` — 删除项目

**响应 (200)：**
```json
{ "code": 200, "msg": "操作成功", "data": null }
```

---

## 5. 消费订单模块 `/api/consumption`

### GET `/api/consumption/page` — 分页查询所有订单

**查询参数：**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |

**请求示例：** `GET /api/consumption/page?page=1&pageSize=10`

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "id": 1,
        "memberId": 1,
        "totalAmount": 168.00,
        "payMethod": 1,
        "payAmount": 168.00,
        "balanceUsed": 0.00,
        "pointsEarned": 168,
        "status": 1,
        "createTime": "2026-05-14 14:00:00",
        "updateTime": "2026-05-14 14:00:00"
      }
    ],
    "total": 30,
    "page": 1,
    "pageSize": 10,
    "pages": 3
  }
}
```

### GET `/api/consumption/{id}` — 查询订单详情

**路径参数：** `id` — 订单ID

**响应 (200)：** 返回单个 ConsumptionOrder 对象，结构同上 `list` 中的元素。

### GET `/api/consumption/member/{memberId}/page` — 按会员查询订单

**路径参数：** `memberId` — 会员ID
**查询参数：** `page`（默认1）、`pageSize`（默认10）

**请求示例：** `GET /api/consumption/member/1/page?page=1&pageSize=10`

**响应 (200)：** 分页结构，list 元素同上。

### GET `/api/consumption/{orderId}/items` — 查询订单明细

**路径参数：** `orderId` — 订单ID

**请求示例：** `GET /api/consumption/1/items`

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "orderId": 1,
      "itemId": 1,
      "itemName": "洗剪吹",
      "itemPrice": 68.00,
      "quantity": 1
    },
    {
      "id": 2,
      "orderId": 1,
      "itemId": 5,
      "itemName": "营养护理",
      "itemPrice": 100.00,
      "quantity": 1
    }
  ]
}
```

### POST `/api/consumption` — 创建消费订单

**请求体：**
```json
{
  "memberId": 1,
  "payMethod": 1,
  "payAmount": 168.00,
  "balanceUsed": 0.00,
  "couponCode": "CPN20260501A001",
  "employeeId": 1,
  "payRemark": "老客户优惠",
  "items": [
    {
      "itemId": 1,
      "itemName": "洗剪吹",
      "itemPrice": 68.00,
      "quantity": 1
    },
    {
      "itemId": 5,
      "itemName": "营养护理",
      "itemPrice": 100.00,
      "quantity": 1
    }
  ]
}
```

**字段说明：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| memberId | Long | 是 | 会员ID |
| payMethod | Integer | 否 | 支付方式：1现金/2余额/3微信/4支付宝/5银行卡/6储值卡/7团购券/8混合 |
| payAmount | BigDecimal | 否 | 支付金额（现金+微信部分） |
| balanceUsed | BigDecimal | 否 | 使用余额（payMethod=2时自动设为订单金额） |
| couponCode | String | 否 | 优惠券码，同一事务内核销，原子防并发 |
| employeeId | Long | 否 | 服务技师ID，传入后自动计算提成 |
| payRemark | String | 否 | 支付备注 |
| items | Array | 是 | 订单明细，至少1项 |
| payments | Array | 否 | 组合支付明细（payMethod=8时使用，合计须等于订单金额） |

**payments 中每个元素：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| payMethod | Integer | 是 | 支付方式 |
| amount | BigDecimal | 是 | 支付金额 |

**items 中每个元素：**
| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| itemId | Long | 是 | — | 服务项目ID |
| itemName | String | 否 | — | 项目名称（冗余） |
| itemPrice | BigDecimal | 否 | — | 项目单价（冗余） |
| quantity | Integer | 否 | 1 | 数量 |

**业务逻辑：**
- 自动根据会员等级计算折扣价
- 优惠券核销与订单创建在同一事务（`UPDATE coupon SET status=2 WHERE id=? AND status=1`）
- 原子扣减余额（`UPDATE member SET balance=balance-? WHERE id=?`）
- 消费完成后自动评估等级升级
- 多条明细单次 IN 查询计算提成

**响应 (200)：** 返回创建的订单完整对象。

---

### PUT `/api/consumption/{id}/refund` — 退款

**请求示例：** `PUT /api/consumption/1/refund`

**业务逻辑：**
- 原子标记退款状态（`UPDATE SET status=2 WHERE status=1`，防并发双重退款）
- 退回余额、积分（原子化 UPDATE）
- 恢复优惠券（status 回退为 1）
- 自动评估等级降级

**响应 (200)：** 返回退款后订单对象（status=2）。
**响应 (400)：** `该订单已退款或状态不符`（防双重退款）

---

### POST `/api/consumption/suspend` — 挂单（B9）

**请求体：**
```json
{
  "memberId": 1,
  "employeeId": 2,
  "items": [
    { "itemId": 1, "itemName": "洗剪吹", "itemPrice": 68.00, "quantity": 1 }
  ]
}
```

**说明：** 创建 status=0 的草稿订单，不处理支付和积分。复用折扣计算和提成计算逻辑。

**响应 (200)：** 返回订单对象（status=0）。

### GET `/api/consumption/suspended` — 查询挂起订单（B9）

**请求示例：** `GET /api/consumption/suspended`

**响应 (200)：**
```json
{
  "code": 200,
  "data": [
    { "id": 7, "memberId": 1, "totalAmount": 68.00, "itemCount": 1, "suspendTime": "2026-05-23 15:03:16" }
  ]
}
```

### PUT `/api/consumption/{id}/resume` — 取单结算（B9）

**请求体：**
```json
{
  "payMethod": 1,
  "payAmount": 68.00,
  "balanceUsed": 0,
  "couponCode": null,
  "payRemark": null,
  "employeeId": 2
}
```

**说明：** 加载挂单（status=0）并完成支付流程（组合支付/优惠券/余额扣减/等级升级），原子状态转换 0→1。

**响应 (200)：** 返回订单对象（status=1）。
**响应 (400)：** `订单已支付，无法重复结算`（防重复结算）

---

## 6. 会员等级模块 `/api/member-level`

### GET `/api/member-level/list` — 查询所有等级

**请求示例：** `GET /api/member-level/list`

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "普通会员",
      "discountRate": 1.00,
      "pointsRequired": 0,
      "sort": 1,
      "createTime": "2026-05-14 10:00:00",
      "updateTime": "2026-05-14 10:00:00"
    },
    {
      "id": 2,
      "name": "银卡会员",
      "discountRate": 0.95,
      "pointsRequired": 200,
      "sort": 2,
      "createTime": "2026-05-14 10:00:00",
      "updateTime": "2026-05-14 10:00:00"
    },
    {
      "id": 3,
      "name": "金卡会员",
      "discountRate": 0.90,
      "pointsRequired": 500,
      "sort": 3,
      "createTime": "2026-05-14 10:00:00",
      "updateTime": "2026-05-14 10:00:00"
    },
    {
      "id": 4,
      "name": "钻石会员",
      "discountRate": 0.85,
      "pointsRequired": 1000,
      "sort": 4,
      "createTime": "2026-05-14 10:00:00",
      "updateTime": "2026-05-14 10:00:00"
    }
  ]
}
```

> ⚠️ `discountRate` 字段类型为 BigDecimal，例如 0.85 表示 85折。**不是** `07-需求文档.md` 中记录的 `discount`(INT) 和 `minPoints`。

### GET `/api/member-level/{id}` — 查询单个等级

**响应 (200)：** 返回单个 MemberLevel 对象。

### POST `/api/member-level` — 新增等级

**请求体：**
```json
{
  "name": "铂金会员",
  "discountRate": 0.80,
  "pointsRequired": 5000,
  "sort": 5
}
```

**响应 (200)：** 返回带 ID 的完整等级对象。

### PUT `/api/member-level/{id}` — 修改等级

**请求体：** 同新增

**响应 (200)：** 返回修改后的等级对象。

### DELETE `/api/member-level/{id}` — 删除等级

**响应 (200)：**
```json
{ "code": 200, "msg": "操作成功", "data": null }
```

---

## 7. 员工模块 `/api/employee`

### GET `/api/employee` — 分页查询员工

**查询参数：**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |

**请求示例：** `GET /api/employee?page=1&pageSize=10`

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "李师傅",
        "phone": "13900139001",
        "position": "高级发型师",
        "salary": 8000.00,
        "status": 1,
        "createTime": "2026-05-14 10:00:00",
        "updateTime": "2026-05-14 10:00:00"
      }
    ],
    "total": 5,
    "page": 1,
    "pageSize": 10,
    "pages": 1
  }
}
```

### GET `/api/employee/{id}` — 查询单个员工

**响应 (200)：** 返回单个 Employee 对象。

### POST `/api/employee` — 新增员工

**请求体：**
```json
{
  "name": "李师傅",
  "phone": "13900139001",
  "position": "高级发型师",
  "salary": 8000.00,
  "status": 1
}
```

**字段说明：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 姓名 |
| phone | String | 否 | 手机号 |
| position | String | 否 | 职位 |
| salary | BigDecimal | 否 | 薪资 |
| status | Integer | 否 | 状态：1在职/0离职 |

**响应 (200)：** 返回带 ID 的完整员工对象。

### PUT `/api/employee/{id}` — 修改员工

**请求体：** 同新增

**响应 (200)：** 返回修改后的员工对象。

### DELETE `/api/employee/{id}` — 删除员工

**响应 (200)：**
```json
{ "code": 200, "msg": "操作成功", "data": null }
```

---

## 8. 仪表盘 `/api/dashboard`

### GET `/api/dashboard/stats` — 获取统计概览

**请求示例：** `GET /api/dashboard/stats`

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "memberCount": 120,
    "todayNewMember": 3,
    "todayOrderCount": 15,
    "todayRevenue": 2680.00,
    "recent7DayOrders": [
      { "date": "2026-05-08", "count": 12 },
      { "date": "2026-05-09", "count": 18 }
    ]
  }
}
```

> 注：此接口返回结构由 `JdbcTemplate` 查询动态生成，具体字段名取决于 SQL 查询的 AS 别名。

### GET `/api/dashboard/recent-orders` — 最近订单

**请求示例：** `GET /api/dashboard/recent-orders`

**响应 (200)：** 返回最近订单列表（具体字段见代码 SQL 查询）。

---

## 9. 预约模块 /api/appointment

### GET /api/appointment/page — 分页查询预约列表

**查询参数：**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| startDate | String | 否 | — | 开始日期 yyyy-MM-dd |
| endDate | String | 否 | — | 结束日期 yyyy-MM-dd |
| status | int | 否 | — | 1已预约 2已到店 3已完成 4已取消 5爽约 |

**响应 (200)：** 分页列表，包含 memberName/serviceItemName/employeeName 关联字段。

### GET /api/appointment/{id} — 查询预约详情

### POST /api/appointment — 新增预约

**请求体：**
{ memberId(必填), serviceItemId, employeeId, appointmentDate(必填), startTime(HH:mm:ss), endTime(HH:mm:ss), duration, status, remark }

### PUT /api/appointment/{id} — 修改预约

**请求体：** 同新增

### PUT /api/appointment/{id}/status — 更新预约状态

**请求体：** `{ "status": 2 }`

**状态机规则：**
| 当前状态 | 允许变更到 | 说明 |
|---------|-----------|------|
| 1（已预约） | 2, 4, 5 | 可到店/取消/爽约 |
| 2（已到店） | 3, 5 | 可完成/爽约 |
| 3/4/5 | — | 终态，不可再变更 |

**响应 (200)：** 操作成功
**响应 (400)：** `无效的状态变更: X → Y`

### PUT `/api/appointment/{id}/arrive` — 预约到店（1→2）

**请求示例：** `PUT /api/appointment/1/arrive`

**业务逻辑：** 校验状态=1后更新为2，同时更新会员 last_visit_date 和 visit_count。

**响应 (200)：** 操作成功
**响应 (400)：** `当前预约状态不可到店`

### DELETE /api/appointment/{id} — 删除预约

### GET /api/appointment/calendar/day — 日历日视图

**查询参数：**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| date | String | 是 | — | 查询日期 yyyy-MM-dd |
| employeeId | Long | 否 | — | 按技师筛选 |
| status | int | 否 | — | 按状态筛选 |

**响应 (200)：**
```json
{
  "technicians": [{ "id", "name", "status": "AVAILABLE", "appointments": [{ "id", "memberName", "serviceItemName", "startTime", "endTime", "status", "remark" }] }],
  "unassignedAppointments": [],
  "stats": { "total", "booked", "arrived", "completed" },
  "businessHours": { "start": "08:00", "end": "21:00" }
}
```

### GET /api/appointment/calendar/week — 日历周视图

**查询参数：**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| startDate | String | 是 | — | 周一日期 yyyy-MM-dd |
| employeeId | Long | 否 | — | 按技师筛选 |

**响应 (200)：**
```json
{
  "days": [{ "date", "dayOfWeek", "totalCount", "statusCounts": { "booked", "arrived", "completed", "cancelled", "noshow" }, "appointments": [...] }]
}
```

---

## 10. 报表模块 /api/report

### GET `/api/report/daily` — 日报表

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| start | String | 否 | 开始日期，格式 `yyyy-MM-dd` |
| end | String | 否 | 结束日期，格式 `yyyy-MM-dd` |

**请求示例：** `GET /api/report/daily?start=2026-05-01&end=2026-05-14`

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "date": "2026-05-14",
      "orderCount": 15,
      "revenue": 2680.00,
      "points": 2680
    }
  ]
}
```

> 注：实际返回字段为 `date`、`orderCount`、`revenue`、`points`（由 `ReportController.java` SQL 查询决定）。按支付方式拆分金额请使用 `/api/report/cashier-daily` 接口。
```

### GET `/api/report/monthly` — 月报表

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| year | String | 否 | 年份，如 `2026` |

**请求示例：** `GET /api/report/monthly?year=2026`

**响应 (200)：** 按月汇总的数据列表。

### GET `/api/report/service-rank` — 服务项目排行榜

**请求示例：** `GET /api/report/service-rank`

**响应 (200)：** 按消费次数降序的服务项目排行列表。

### GET `/api/report/cashier-daily` — 收银日报

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| date | String | 否 | 日期，格式 `yyyy-MM-dd`，不传则查全部 |

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    { "payMethod": "现金", "orderCount": 3, "totalAmount": 500.00, "payAmount": 500.00, "balanceUsed": 0.00 },
    { "payMethod": "微信", "orderCount": 5, "totalAmount": 980.00, "payAmount": 980.00, "balanceUsed": 0.00 },
    { "payMethod": "余额", "orderCount": 2, "totalAmount": 200.00, "payAmount": 0.00, "balanceUsed": 200.00 }
  ]
}
```

### GET `/api/report/employee-performance` — 员工业绩报表

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| start | String | 否 | 开始日期 `yyyy-MM-dd` |
| end | String | 否 | 结束日期 `yyyy-MM-dd` |

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    { "id": 1, "name": "刘技师", "orderCount": 25, "totalRevenue": 6800.00, "serviceCount": 30 }
  ]
}
```

### GET `/api/report/member-trend` — 会员获取趋势（2026-05-24 新增）

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    { "period": "2025-07", "newMembers": 12 },
    { "period": "2025-08", "newMembers": 18 }
  ]
}
```

> 返回最近 12 个月每月新增会员数。

### GET `/api/report/coupon-usage` — 优惠券使用率（2026-05-24 新增）

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    { "templateName": "新客专享50元券", "issuedQty": 100, "usedQty": 35, "usageRate": "35.00%" }
  ]
}
```

---

## 10b. API 路径对照速查表（管理端）

| 模块 | 方法 | 完整路径 | 认证 |
|------|------|----------|------|
| 认证 | POST | `/api/auth/login` | 否 |
| 会员 | GET | `/api/member/page` | 是 |
| 会员 | GET | `/api/member/{id}` | 是 |
| 会员 | POST | `/api/member` | 是 |
| 会员 | PUT | `/api/member/{id}` | 是 |
| 会员 | DELETE | `/api/member/{id}` | 是 |
| 服务分类 | GET | `/api/service-category` | 是 |
| 服务分类 | POST | `/api/service-category` | 是 |
| 服务分类 | PUT | `/api/service-category/{id}` | 是 |
| 服务分类 | DELETE | `/api/service-category/{id}` | 是 |
| 服务项目 | GET | `/api/service-item` | 是 |
| 服务项目 | GET | `/api/service-item/category/{categoryId}` | 是 |
| 服务项目 | POST | `/api/service-item` | 是 |
| 服务项目 | PUT | `/api/service-item/{id}` | 是 |
| 服务项目 | DELETE | `/api/service-item/{id}` | 是 |
| 消费订单 | GET | `/api/consumption/page` | 是 |
| 消费订单 | GET | `/api/consumption/{id}` | 是 |
| 消费订单 | GET | `/api/consumption/member/{memberId}/page` | 是 |
| 消费订单 | GET | `/api/consumption/{orderId}/items` | 是 |
| 消费订单 | POST | `/api/consumption` | 是 |
| 会员等级 | GET | `/api/member-level/list` | 是 |
| 会员等级 | GET | `/api/member-level/{id}` | 是 |
| 会员等级 | POST | `/api/member-level` | 是 |
| 会员等级 | PUT | `/api/member-level/{id}` | 是 |
| 会员等级 | DELETE | `/api/member-level/{id}` | 是 |
| 员工 | GET | `/api/employee` | 是 |
| 员工 | GET | `/api/employee/{id}` | 是 |
| 员工 | POST | `/api/employee` | 是 |
| 员工 | PUT | `/api/employee/{id}` | 是 |
| 员工 | DELETE | `/api/employee/{id}` | 是 |
| 仪表盘 | GET | `/api/dashboard/stats` | 是 |
| 仪表盘 | GET | `/api/dashboard/recent-orders` | 是 |
| 预约 | GET | `/api/appointment/page` | 是 |
| 预约 | GET | `/api/appointment/{id}` | 是 |
| 预约 | POST | `/api/appointment` | 是 |
| 预约 | PUT | `/api/appointment/{id}` | 是 |
| 预约 | PUT | `/api/appointment/{id}/status` | 是 |
| 预约 | DELETE | `/api/appointment/{id}` | 是 |
| 报表 | GET | `/api/report/daily` | 是 |
| 报表 | GET | `/api/report/monthly` | 是 |
| 报表 | GET | `/api/report/service-rank` | 是 |
| 报表 | GET | `/api/report/cashier-daily` | 是 |
| 报表 | GET | `/api/report/employee-performance` | 是 |
| 商品分类 | GET | `/api/product-category` | 是 |
| 商品分类 | POST | `/api/product-category` | 是 |
| 商品分类 | PUT | `/api/product-category/{id}` | 是 |
| 商品分类 | DELETE | `/api/product-category/{id}` | 是 |
| 商品 | GET | `/api/product/page` | 是 |
| 商品 | GET | `/api/product/low-stock` | 是 |
| 商品 | GET | `/api/product/{id}` | 是 |
| 商品 | POST | `/api/product` | 是 |
| 商品 | PUT | `/api/product/{id}` | 是 |
| 商品 | DELETE | `/api/product/{id}` | 是 |
| 供应商 | GET | `/api/supplier` | 是 |
| 供应商 | POST | `/api/supplier` | 是 |
| 供应商 | PUT | `/api/supplier/{id}` | 是 |
| 供应商 | DELETE | `/api/supplier/{id}` | 是 |
| 库存流水 | GET | `/api/stock-record/page` | 是 |
| 库存流水 | POST | `/api/stock-record` | 是 |
| 采购订单 | GET | `/api/purchase-order/page` | 是 |
| 采购订单 | GET | `/api/purchase-order/{id}` | 是 |
| 采购订单 | POST | `/api/purchase-order` | 是 |
| 采购订单 | PUT | `/api/purchase-order/{id}` | 是 |
| 采购订单 | PUT | `/api/purchase-order/{id}/submit` | 是 |
| 采购订单 | PUT | `/api/purchase-order/{id}/approve` | 是 |
| 采购订单 | PUT | `/api/purchase-order/{id}/receive` | 是 |
| 采购订单 | PUT | `/api/purchase-order/{id}/cancel` | 是 |
| 采购订单 | DELETE | `/api/purchase-order/{id}` | 是 |
| 退货管理 | GET | `/api/return-order/page` | 是 |
| 退货管理 | GET | `/api/return-order/{id}` | 是 |
| 退货管理 | POST | `/api/return-order` | 是 |
| 退货管理 | PUT | `/api/return-order/{id}` | 是 |
| 退货管理 | PUT | `/api/return-order/{id}/submit` | 是 |
| 退货管理 | PUT | `/api/return-order/{id}/approve` | 是 |
| 退货管理 | PUT | `/api/return-order/{id}/reject` | 是 |
| 退货管理 | PUT | `/api/return-order/{id}/complete` | 是 |
| 退货管理 | PUT | `/api/return-order/{id}/cancel` | 是 |
| 退货管理 | DELETE | `/api/return-order/{id}` | 是 |
| 消费订单 | PUT | `/api/consumption/{id}/refund` | 是 |
| 次卡 | GET | `/api/service-card/page` | 是 |
| 次卡 | POST | `/api/service-card` | 是 |
| 次卡 | GET | `/api/service-card/{id}` | 是 |
| 次卡 | DELETE | `/api/service-card/{id}` | 是 |
| 会员 | GET | `/api/member/churn-risk?days=60` | 是 |
| 会员 | GET | `/api/member/export` | 是 |
| 会员 | POST | `/api/member/import` | 是 |
| 班次模板 | GET | `/api/shift-template` | 是 |
| 班次模板 | POST | `/api/shift-template` | 是 |
| 班次模板 | PUT | `/api/shift-template/{id}` | 是 |
| 班次模板 | DELETE | `/api/shift-template/{id}` | 是 |
| 排班 | GET | `/api/schedule/week` | 是 |
| 排班 | POST | `/api/schedule/batch` | 是 |
| 排班 | DELETE | `/api/schedule/{id}` | 是 |
| 考勤 | GET | `/api/attendance/page` | 是 |
| 考勤 | POST | `/api/attendance/clock` | 是 |
| 管理员 | GET | `/api/admin` | 是 |
| 管理员 | POST | `/api/admin` | 是 |
| 管理员 | PUT | `/api/admin/{id}` | 是 |
| 管理员 | DELETE | `/api/admin/{id}` | 是 |
| 优惠券模板 | GET | `/api/coupon-template` | 是 |
| 优惠券模板 | GET | `/api/coupon-template/{id}` | 是 |
| 优惠券模板 | POST | `/api/coupon-template` | 是 |
| 优惠券模板 | PUT | `/api/coupon-template/{id}` | 是 |
| 优惠券模板 | DELETE | `/api/coupon-template/{id}` | 是 |
| 优惠券 | GET | `/api/coupon/page` | 是 |
| 优惠券 | POST | `/api/coupon/issue` | 是 |
| 优惠券 | POST | `/api/coupon/verify` | 是 |
| 会员充值 | POST | `/api/member/{id}/recharge` | 是 |
| 次卡扣减 | PUT | `/api/service-card/{id}/deduct` | 是 |
| 预约转消费 | POST | `/api/appointment/{id}/convert-to-order` | 是 |
| 报表 | GET | `/api/report/member-spending` | 是 |
| 提成结算 | GET | `/api/commission/settlements` | 是 |
| 提成结算 | GET | `/api/commission/settlements/{id}` | 是 |
| 提成结算 | POST | `/api/commission/settlements` | 是 |
| 提成结算 | PUT | `/api/commission/settlements/{id}/confirm` | 是 |
| 提成结算 | PUT | `/api/commission/settlements/{id}/pay` | 是 |
| 提成结算 | DELETE | `/api/commission/settlements/{id}` | 是 |
| 拼团模板 | GET | `/api/group-buy/templates` | 是 |
| 拼团模板 | POST | `/api/group-buy/templates` | 是 |
| 拼团模板 | PUT | `/api/group-buy/templates/{id}` | 是 |
| 拼团模板 | PUT | `/api/group-buy/templates/{id}/status` | 是 |
| 拼团团单 | GET | `/api/group-buy/orders` | 是 |
| 拼团团单 | GET | `/api/group-buy/orders/{id}` | 是 |
| 拼团团单 | PUT | `/api/group-buy/participants/{id}/redeem` | 是 |

---

## 11. 库存管理 API

### 11.1 商品分类

**GET /api/product-category** — 获取全部分类

> 响应：`{ "code": 200, "data": [{ "id": 1, "name": "染膏", "sort": 1 }] }`

**POST /api/product-category** — 新增分类

> 请求：`{ "name": "染膏", "sort": 1 }`

**PUT /api/product-category/{id}** — 修改分类

**DELETE /api/product-category/{id}** — 删除分类

### 11.2 商品

**GET /api/product/page?page=1&pageSize=10&categoryId=&keyword=** — 分页列表（含分类名、供应商名）

**GET /api/product/low-stock?page=1&pageSize=10** — 库存预警（stock_qty <= alert_qty）

**GET /api/product/{id}** — 商品详情

**POST /api/product** — 新增商品

> 请求：`{ "categoryId": 1, "name": "欧莱雅染膏", "unit": "支", "salePrice": 68, "stockQty": 5, "alertQty": 10, "status": 1 }`

**PUT /api/product/{id}** — 修改商品

**DELETE /api/product/{id}** — 删除商品

### 11.3 供应商

**GET /api/supplier** — 获取全部供应商

**POST /api/supplier** — 新增

> 请求：`{ "name": "美发用品批发", "contact": "张总", "phone": "13800001111" }`

**PUT /api/supplier/{id}** — 修改

**DELETE /api/supplier/{id}** — 删除

### 11.4 库存流水

**GET /api/stock-record/page?page=1&pageSize=10&type=1** — 分页列表（type: 1入库 / 2出库 / 3退货）

**POST /api/stock-record** — 入库/出库（自动更新 product.stock_qty）

> 请求：`{ "productId": 1, "type": 1, "qty": 20, "price": 35, "totalAmount": 700, "supplierId": 1, "remark": "进货" }`
>
> 出库库存不足时返回 400：`{ "code": 400, "msg": "库存不足，当前库存：5" }`

### 11.5 采购订单

**GET /api/purchase-order/page?page=1&pageSize=10&status=0&supplierId=1** — 分页列表（status: 0草稿/1已提交/2已审批/3已收货/4已取消）

**GET /api/purchase-order/{id}** — 订单详情（含明细行和供应商名）

**POST /api/purchase-order** — 创建采购订单（草稿）

> 请求：`{ "supplierId": 1, "items": [{ "productId": 1, "productName": "洗发水", "unit": "瓶", "qty": 10, "price": 25.00 }], "remark": "首次采购" }`
>
> 响应：`{ "code": 200, "data": { "id": 1, "orderNo": "PO-20260523-12345", "status": 0, ... } }`

**PUT /api/purchase-order/{id}** — 编辑草稿（仅 status=0）

**PUT /api/purchase-order/{id}/submit** — 提交审核（0→1，原子 SQL：`WHERE status=0`）

**PUT /api/purchase-order/{id}/approve** — 审核通过（1→2，原子 SQL：`WHERE status=1`，记录审批人+时间）

**PUT /api/purchase-order/{id}/receive** — 确认收货（2→3，原子 SQL：`WHERE status=2`，自动：逐行 `UPDATE product SET stock_qty + qty` + 插入 `stock_record` type=1）

**PUT /api/purchase-order/{id}/cancel** — 取消（0→4 或 1→4）

**DELETE /api/purchase-order/{id}** — 删除（仅 status=0）

### 11.6 退货管理

**GET /api/return-order/page?page=1&pageSize=10&status=0&supplierId=1** — 分页列表（status: 0草稿/1已提交/2已审批/3已完成/4已驳回）

**GET /api/return-order/{id}** — 订单详情（含明细行和供应商名）

**POST /api/return-order** — 创建退货订单（草稿）

> 请求：`{ "supplierId": 1, "items": [{ "productId": 1, "productName": "洗发水", "unit": "瓶", "qty": 3, "price": 25.00 }], "reason": "包装破损", "remark": "急处理" }`
>
> 响应：`{ "code": 200, "data": { "id": 1, "orderNo": "RO-20260523-12345", "status": 0, ... } }`

**PUT /api/return-order/{id}** — 编辑草稿（仅 status=0）

**PUT /api/return-order/{id}/submit** — 提交审核（0→1）

**PUT /api/return-order/{id}/approve** — 审核通过（1→2）

**PUT /api/return-order/{id}/reject** — 驳回（1→4）

**PUT /api/return-order/{id}/complete** — 完成退货（2→3，自动：逐行 `UPDATE product SET stock_qty - qty WHERE stock_qty >= qty` + 插入 `stock_record` type=3）

**PUT /api/return-order/{id}/cancel** — 取消（仅 status=0）

**DELETE /api/return-order/{id}** — 删除（仅 status=0）

---

## 12. 与旧文档的关键差异

| 事项 | 旧文档记录 | 实际代码 | 
|------|-----------|----------|
| 服务分类路径 | `/api/service/category/list` | `GET /api/service-category` |
| 服务项目路径 | `/api/service/item/list` | `GET /api/service-item` |
| 员工查询 | `GET /api/employee/page`（分页） | `GET /api/employee?page=&pageSize=`（分页） |
| MemberLevel 折扣字段 | `discount` INT（如 90） | `discountRate` BigDecimal（如 0.90） |
| MemberLevel 积分字段 | `minPoints` INT | `pointsRequired` Integer |
| MemberLevel 排序字段 | 未提及 | `sort` Integer |
| 报表接口 | 仅 `daily` | `daily` + `monthly` + `service-rank` |
| 仪表盘接口 | 仅 `stats` | `stats` + `recent-orders` |

---

## 13. 技师状态模块 `/api/technician-status`（Phase 7）

### GET `/api/technician-status/list` — 所有技师状态

**响应 (200)：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "employeeId": 1,
      "employeeName": "刘师傅",
      "status": "BUSY",
      "currentCustomerName": "张三",
      "currentServiceName": "洗剪吹",
      "lastStatusChanged": "2026-05-19 14:30:00"
    }
  ]
}
```

### GET `/api/technician-status/{employeeId}` — 单个技师详情

### POST `/api/technician-status/change` — 变更技师状态

**请求体：**
```json
{
  "employeeId": 1,
  "status": "BUSY",
  "currentCustomerName": "张三",
  "currentServiceName": "洗剪吹"
}
```

**字段说明：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 是 | 员工ID |
| status | String | 是 | AVAILABLE / BUSY / BREAK / OFF_DUTY |
| currentCustomerName | String | 否 | 当前服务客人 |
| currentServiceName | String | 否 | 当前服务项目 |

### GET `/api/technician-status/performance` — 技师今日业绩统计

**响应 (200)：**
```json
{
  "code": 200,
  "data": [
    {
      "employeeId": 1,
      "employeeName": "刘师傅",
      "todayOrders": 5,
      "todayRevenue": 680.00
    }
  ]
}
```
> 仅包含在职员工(status=1)。数据来源：consumption_order (status=1, DATE=今天) LEFT JOIN employee。

---

## 14. 轮牌排队模块 `/api/service-queue`（Phase 7）

### GET `/api/service-queue/list` — 当前排队列表

**响应 (200)：** 包含等待中 + 已分配的排队记录列表。

### POST `/api/service-queue/enqueue` — 会员入队

**请求体：**
```json
{
  "memberId": 1,
  "memberName": "张三",
  "serviceItemId": 3,
  "serviceItemName": "洗剪吹"
}
```

**字段说明：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| memberId | Long | 是 | 会员ID |
| memberName | String | 是 | 会员姓名 |
| serviceItemId | Long | 否 | 期望服务项目ID |
| serviceItemName | String | 否 | 期望服务项目名 |

### POST `/api/service-queue/assign` — 分配技师

**请求体：**
```json
{
  "queueId": 1,
  "employeeId": 2
}
```

**业务逻辑：** 校验排队状态=等待中 + 技师状态=AVAILABLE → 更新排队为已分配 → 技师变 BUSY → 自动创建 ServiceTimer。全程 @Transactional。

### POST `/api/service-queue/{id}/skip` — 跳过当前

### POST `/api/service-queue/{id}/cancel` — 取消排队

### GET `/api/service-queue/today-stats` — 今日排队统计

**响应 (200)：**
```json
{
  "code": 200,
  "data": {
    "totalQueued": 15,
    "waiting": 3,
    "assigned": 10,
    "cancelled": 2
  }
}
```

---

## 15. 服务计时模块 `/api/service-timer`（Phase 7）

### POST `/api/service-timer/start` — 开始计时

**请求体：**
```json
{
  "appointmentId": null,
  "queueId": 5,
  "employeeId": 2,
  "memberId": 1,
  "serviceItemId": 3,
  "serviceItemName": "烫染",
  "plannedDuration": 60
}
```

**字段说明：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| queueId | Long | 否 | 关联排队ID |
| appointmentId | Long | 否 | 关联预约ID |
| employeeId | Long | 是 | 技师ID |
| memberId | Long | 是 | 会员ID |
| serviceItemId | Long | 否 | 服务项目ID |
| serviceItemName | String | 是 | 服务项目名 |
| plannedDuration | Integer | 否 | 计划时长（分钟），默认 30 |

### POST `/api/service-timer/{id}/pause` — 暂停计时

### POST `/api/service-timer/{id}/resume` — 继续计时

> 恢复时自动修正 startedAt，补偿暂停时长。

### POST `/api/service-timer/{id}/complete` — 完成计时

> 计算 actualDuration，恢复技师为 AVAILABLE。

### GET `/api/service-timer/active` — 当前所有进行中的计时

---

### E8 — 计时完成事件与排队通知（内部机制，无公开 API）

> 计时完成后的内部事件驱动机制，不暴露 HTTP 端点。

**流程：**
1. `ServiceTimerServiceImpl.complete()` 执行后发布 `TimerCompletedEvent`（Spring ApplicationEvent）
2. `QueueNotificationListener` 异步监听该事件（`@Async("notificationExecutor")`）
3. 监听器查询当前等待队列中的下一位会员 → 通过 `SmsService.send()` 发送排队通知短信（`NotificationType.QUEUE_NOTIFY`）
4. 逐条 try-catch，失败不影响计时完成主流程；无等待会员时静默跳过

**事件定义：**
```java
public record TimerCompletedEvent(
    Long timerId,        // 完成的计时ID
    Long employeeId,     // 释放的技师ID
    Long memberId,       // 被服务会员ID
    String serviceName   // 完成的服务项目名
) {}
```

**复用基础设施：** `SmsService`、`notification_log` 表、`NotificationType.QUEUE_NOTIFY` 枚举。

---

## 16. 财务日结模块 `/api/daily-close`（Phase 7）

### GET `/api/daily-close/today` — 今日系统汇总

**响应 (200)：**
```json
{
  "code": 200,
  "data": {
    "closeDate": "2026-05-19",
    "systemCash": 1200.00,
    "systemWechat": 980.00,
    "systemAlipay": 0.00,
    "systemBalance": 500.00,
    "systemCard": 0.00,
    "systemTotal": 2680.00,
    "manualCash": null,
    "manualWechat": null,
    "manualAlipay": null,
    "manualTotal": null,
    "diffAmount": 0.00,
    "status": 0
  }
}
```

### POST `/api/daily-close/save` — 提交人工录入

**请求体：**
```json
{
  "closeDate": "2026-05-19",
  "manualCash": 1200.00,
  "manualWechat": 980.00,
  "manualAlipay": 0.00,
  "remark": "今日正常"
}
```

### POST `/api/daily-close/{id}/lock` — 锁定日结

> 锁定后不可再修改（status=1）。

### GET `/api/daily-close/history?page=1&pageSize=10` — 历史日结列表

### GET `/api/daily-close/{id}` — 日结详情

---

## 17. 标签规则模块 `/api/tag-rules`（Phase 7）

### GET `/api/tag-rules` — 查询所有标签规则

**响应 (200)：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "流失风险客户",
      "tagName": "流失风险",
      "conditionsJson": "[{\"field\":\"last_visit_days\",\"op\":\">\",\"value\":60}]",
      "enabled": 1
    }
  ]
}
```

### POST `/api/tag-rules` — 新增规则

**请求体：**
```json
{
  "name": "流失风险客户",
  "tagName": "流失风险",
  "conditionsJson": "[{\"field\":\"last_visit_days\",\"op\":\">\",\"value\":60}]",
  "enabled": 1
}
```

**条件支持的字段：** last_visit_days, total_spent, visit_count, balance, level
**条件支持的操作符：** `>`, `<`, `>=`, `<=`, `=`, `!=`

### PUT `/api/tag-rules/{id}` — 修改规则

### DELETE `/api/tag-rules/{id}` — 删除规则

### POST `/api/tag-rules/{id}/toggle` — 切换启用/禁用

---

## 18. 通知中心模块 `/api/notifications`（2026-05-24 新增）

### GET `/api/notifications` — 分页查询通知列表

**参数：** `page` (默认1), `pageSize` (默认10)

**响应 (200)：**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1, "memberId": 1, "phone": "138****0000",
        "type": "BIRTHDAY", "content": "尊敬的会员，祝您生日快乐！",
        "status": 1, "isRead": 0, "sentAt": "2026-05-24 09:00:00",
        "createTime": "2026-05-24 09:00:00"
      }
    ],
    "total": 100, "page": 1, "pageSize": 10
  }
}
```

### GET `/api/notifications/unread-count` — 未读通知数量

**响应 (200)：** `{ "code": 200, "data": 5 }`

### PUT `/api/notifications/{id}/read` — 标记单条已读

### PUT `/api/notifications/read-all` — 全部标记已读

**数据来源：** `notification_log` 表（SMS 发送日志），新增 `is_read` 列（0=未读 1=已读）。

---

## 19. Phase 7 API 路径速查

| 模块 | 方法 | 完整路径 | 认证 |
|------|------|----------|------|
| 技师状态 | GET | `/api/technician-status/list` | 是 |
| 技师状态 | GET | `/api/technician-status/{employeeId}` | 是 |
| 技师状态 | POST | `/api/technician-status/change` | 是 |
| 技师状态 | GET | `/api/technician-status/performance` | 是 |
| 通知中心 | GET | `/api/notifications` | 是 |
| 通知中心 | GET | `/api/notifications/unread-count` | 是 |
| 通知中心 | PUT | `/api/notifications/{id}/read` | 是 |
| 通知中心 | PUT | `/api/notifications/read-all` | 是 |
| 轮牌排队 | GET | `/api/service-queue/list` | 是 |
| 轮牌排队 | POST | `/api/service-queue/enqueue` | 是 |
| 轮牌排队 | POST | `/api/service-queue/assign` | 是 |
| 轮牌排队 | POST | `/api/service-queue/{id}/skip` | 是 |
| 轮牌排队 | POST | `/api/service-queue/{id}/cancel` | 是 |
| 轮牌排队 | GET | `/api/service-queue/today-stats` | 是 |
| 服务计时 | POST | `/api/service-timer/start` | 是 |
| 服务计时 | POST | `/api/service-timer/{id}/pause` | 是 |
| 服务计时 | POST | `/api/service-timer/{id}/resume` | 是 |
| 服务计时 | POST | `/api/service-timer/{id}/complete` | 是 |
| 服务计时 | GET | `/api/service-timer/active` | 是 |
| 财务日结 | GET | `/api/daily-close/today` | 是 |
| 财务日结 | POST | `/api/daily-close/save` | 是 |
| 财务日结 | POST | `/api/daily-close/{id}/lock` | 是 |
| 财务日结 | GET | `/api/daily-close/history` | 是 |
| 财务日结 | GET | `/api/daily-close/{id}` | 是 |
| 标签规则 | GET | `/api/tag-rules` | 是 |
| 标签规则 | POST | `/api/tag-rules` | 是 |
| 标签规则 | PUT | `/api/tag-rules/{id}` | 是 |
| 标签规则 | DELETE | `/api/tag-rules/{id}` | 是 |
| 标签规则 | POST | `/api/tag-rules/{id}/toggle` | 是 |

---

## 19. AI 智能搜索模块 `/api/ai`（Phase 5）

**架构：** Spring Boot `AiController` (BFF) → RestClient → Python FastAPI `server.py:8000`

### GET `/api/ai/health` — AI 服务健康检查

**请求示例：** `GET /api/ai/health`

**响应 (200)：**
```json
{
  "code": 200,
  "data": {
    "status": "ok",
    "vectors": 32
  }
}
```

**失败响应 (503)：**
```json
{ "code": 503, "msg": "AI 服务不可用: Connection refused", "data": null }
```

### GET `/api/ai/search` — 语义搜索会员

**查询参数：**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| q | String | 是 | — | 自然语言查询，如"经常烫染的长发女性" |
| topK | int | 否 | 5 | 返回最匹配的 Top-K 条结果 |

**请求示例：** `GET /api/ai/search?q=高消费常客&topK=5`

**响应 (200)：**
```json
{
  "code": 200,
  "data": {
    "results": [
      { "memberId": 1, "name": "张三", "description": "累计消费 5000 元...", "similarity": 0.95 }
    ],
    "queryTimeMs": 12
  }
}
```

**失败响应 (503)：**
```json
{ "code": 503, "msg": "AI 搜索失败: Read timed out", "data": null }
```

> **超时配置：** RestClient 连接超时 5s，读取超时 30s。AI 服务不可用时返回 503 友好降级。

---

## 20. 顾客端模块（Phase 8）

> 顾客端使用独立的 `X-Customer-Token` 认证机制，与管理端 `Authorization: Bearer <jwt>` 互不干扰。

### 20.1 顾客认证 `/api/customer/auth`

#### POST `/api/customer/auth/send-code` — 发送验证码

**请求体：**
```json
{ "phone": "13800138001" }
```

**说明：** 验证码存入 `notification_log` 表，Mock 模式下直接打印日志。限流：1 次/分钟/IP。

#### POST `/api/customer/auth/login` — 顾客登录

**请求体：**
```json
{
  "phone": "13800138001",
  "code": "123456"
}
```

**响应 (200)：**
```json
{
  "code": 200,
  "data": {
    "token": "uuid-customer-token",
    "memberId": 1,
    "name": "张三",
    "phone": "13800138001"
  }
}
```

**说明：** Token 7 天有效期，存储于 `customer_session` 表。

#### GET `/api/customer/auth/me` — 获取当前登录信息

**请求头：** `X-Customer-Token: <token>`

**响应 (200)：** 返回 Member 对象（不含 password 字段）。

#### POST `/api/customer/auth/logout` — 退出登录

**请求头：** `X-Customer-Token: <token>`

---

### 20.2 顾客支付 `/api/customer/payment`

> **当前状态：** Mock 网关实现完成，真实 SDK 待商户审核通过后切换。

#### POST `/api/customer/payment/create` — 创建支付

**请求头：** `X-Customer-Token: <token>`（必填）

**请求体：**
```json
{
  "orderId": 1,
  "method": "WECHAT"
}
```

**响应 (200)：**
```json
{
  "code": 200,
  "data": {
    "orderId": 1,
    "qrCodeUrl": "weixin://wxpay/bizpayurl?pr=...",
    "status": "PENDING"
  }
}
```

#### GET `/api/customer/payment/{id}/status` — 查询支付状态

**请求头：** `X-Customer-Token: <token>`（必填）

**响应 (200)：** `{ "status": "PENDING" }` | `{ "status": "SUCCESS" }` | `{ "status": "FAILED" }`

---

### 20.3 顾客门户 `/api/customer/portal`（I5 + I6）

> 2026-05-23 新增。顾客端个人中心、充值购卡、服务进度查询。

#### GET `/api/customer/portal/profile` — 获取个人资料

**请求头：** `X-Customer-Token: <token>`（必填）

**响应 (200)：** 返回 MemberVO（id, name, phone, balance, points, level 等）。

#### PUT `/api/customer/portal/profile` — 修改个人资料

**请求头：** `X-Customer-Token: <token>`（必填）
**请求体：** `{ "name": "新昵称", "birthday": "1990-01-01" }`

#### GET `/api/customer/portal/orders` — 我的消费记录

**请求头：** `X-Customer-Token: <token>`（必填）
**查询参数：** `page`（默认1）、`size`（默认10）
**响应 (200)：** 分页返回 ConsumptionOrderVO。

#### GET `/api/customer/portal/orders/{id}` — 消费详情

**请求头：** `X-Customer-Token: <token>`（必填）

#### GET `/api/customer/portal/coupons` — 我的优惠券

**请求头：** `X-Customer-Token: <token>`（必填）
**响应 (200)：** 返回 CouponVO 列表。

#### POST `/api/customer/portal/recharge` — 顾客充值（I5）

**请求头：** `X-Customer-Token: <token>`（必填）
**请求体：**
```json
{ "amount": 200.00, "payMethod": 3 }
```
**字段说明：** `payMethod`: 3=微信 / 4=支付宝。
**说明：** 后端委托 `MemberService.recharge(memberId, dto)`。

#### GET `/api/customer/portal/service-cards` — 我的次卡（I5）

**请求头：** `X-Customer-Token: <token>`（必填）
**响应 (200)：** 返回 ServiceCardVO 列表（id, serviceItemName, totalCount, remainingCount, status 等）。

#### POST `/api/customer/portal/service-cards/purchase` — 购买次卡（I5）

**请求头：** `X-Customer-Token: <token>`（必填）
**请求体：**
```json
{ "serviceItemId": 1, "totalCount": 10 }
```
**说明：** 从 `service_item` 查询单价计算总价，调用 `ServiceCardService.purchase()` 完成原子余额扣减。

#### GET `/api/customer/portal/balance-records` — 余额变动记录（I5）

**请求头：** `X-Customer-Token: <token>`（必填）
**响应 (200)：** 返回 RechargeRecord 列表（BalanceRecordVO）。

#### GET `/api/customer/portal/progress` — 服务进度（I6）

**请求头：** `X-Customer-Token: <token>`（必填）

**说明：** 查询逻辑：先查 `service_timer`（status IN 1,2）→ IN_SERVICE；再查 `service_queue`（status=1）→ WAITING；否则 → IDLE。

**响应 (200) — 服务中：**
```json
{
  "status": "IN_SERVICE",
  "technicianName": "张师傅",
  "serviceName": "洗剪吹",
  "elapsedSeconds": 720,
  "remainingSeconds": 1080,
  "plannedDuration": 30,
  "timerStatus": "RUNNING"
}
```
**响应 (200) — 排队中：**
```json
{
  "status": "WAITING",
  "queueNumber": 5,
  "waitingAhead": 3
}
```
**响应 (200) — 空闲：**
```json
{ "status": "IDLE" }
```

#### GET `/api/customer/portal/slots` — 查询可用预约时段

**查询参数：** `date` (YYYY-MM-DD, 必填), `serviceItemId` (必填)
**认证：** 无需 Token
**说明：** 查询服务项目时长，检查当天已有预约（status IN 1,2）的时间冲突，返回 09:00-17:30 每半小时时段的可用性。

**响应 (200)：**
```json
[
  {"time": "09:00", "available": true},
  {"time": "09:30", "available": false},
  ...
  {"time": "17:30", "available": true}
]
```

#### GET `/api/customer/portal/my-appointments` — 我的预约

**请求头：** `X-Customer-Token: <token>`（必填）

**响应 (200)：** 返回当前顾客的所有预约记录，按预约日期+时段降序排列。

#### POST `/api/customer/portal/appointments/{id}/cancel` — 取消预约

**请求头：** `X-Customer-Token: <token>`（必填）
**限制：** 仅可取消 status=1 的本人预约（原子校验：`WHERE id=? AND member_id=? AND status=1`）。

---

### 20.4 支付回调 `/api/payment/callback`（白名单，无需认证）

> 第三方支付平台异步通知。第一步校验幂等（`transaction_id` 去重），验签后更新 `payment_detail` 状态并发布 `PaymentSuccessEvent`。
> 回调返回格式必须遵守各平台规范（微信 `{"code":"SUCCESS","message":"OK"}`，支付宝 `"success"` / `"fail"`），不能包装为 `Result<>`。

#### POST `/api/payment/callback/wechat` — 微信支付回调

**请求头：** `Wechatpay-Signature: <签名>`（可选，Mock 忽略）

**请求体：** 微信支付 V3 回调 JSON（含 `resource` 加密数据），当前 Mock 接收任意 JSON。

**幂等机制：** 从请求体提取 `transaction_id`，内存 `ConcurrentHashMap` 去重（生产需换 Redis SETNX）。

**处理流程：**
1. 提取 `transaction_id` → 检查 `processedCallbacks` 去重
2. 调用 `wechatPayGateway.verifyCallback(body, signature)` 验签
3. 验签通过 → `eventPublisher.publishEvent(new PaymentSuccessEvent(...))`
4. `PaymentNotificationListener` 异步监听 → 查会员手机号 → 发送支付成功短信

**响应：**
```json
{"code": "SUCCESS", "message": "OK"}
```
验签失败返回：`{"code": "FAIL", "message": "签名验证失败"}`

#### POST `/api/payment/callback/alipay` — 支付宝支付回调

**请求体：** 支付宝回调表单数据（含 `out_trade_no`），当前 Mock 接收任意内容。

**处理流程：** 同微信回调。从请求体提取 `out_trade_no` 作为幂等键。

**响应：** 成功 `"success"`，失败 `"fail"`（纯文本，非 JSON）。

---

### 20.5 支付成功事件与通知

> 支付回调验签通过后，`PaymentCallbackController.publishPaymentSuccess()` 发布 `PaymentSuccessEvent`（Spring ApplicationEvent），由 `PaymentNotificationListener` 异步消费。

**事件定义：** `payment/event/PaymentSuccessEvent.java`
```java
public record PaymentSuccessEvent(
    Long paymentId,    // 支付明细ID（Mock 阶段为 null）
    Long orderId,      // 订单ID（Mock 阶段为 null）
    Long memberId,     // 会员ID（Mock 阶段为 null）
    BigDecimal amount, // 支付金额（Mock 阶段为 null）
    String payChannel  // 支付渠道：WECHAT / ALIPAY
) {}
```

**监听器：** `notification/listener/PaymentNotificationListener.java`
- `@Async("notificationExecutor")` — 不阻塞回调响应
- 通过 `memberId` 查询会员手机号 → 调用 `smsService.send()` 发送支付成功通知
- 逐条 try-catch，失败不影响回调主流程
- `memberId` 为 null 或会员无手机号时静默跳过

---

## 21. 顾客端 + AI API 路径速查

| 模块 | 方法 | 完整路径 | 认证 |
|------|------|----------|------|
| AI 搜索 | GET | `/api/ai/health` | 是 |
| AI 搜索 | GET | `/api/ai/search` | 是 |
| 顾客认证 | POST | `/api/customer/auth/send-code` | 否 |
| 顾客认证 | POST | `/api/customer/auth/login` | 否 |
| 顾客认证 | GET | `/api/customer/auth/me` | Customer Token |
| 顾客认证 | POST | `/api/customer/auth/logout` | Customer Token |
| 顾客支付 | POST | `/api/customer/payment/create` | Customer Token |
| 顾客支付 | GET | `/api/customer/payment/{id}/status` | Customer Token |
| 支付回调 | POST | `/api/payment/callback/wechat` | 否（验签） |
| 支付回调 | POST | `/api/payment/callback/alipay` | 否（验签） |
| 顾客门户 | GET | `/api/customer/portal/my-appointments` | Customer Token |
| 顾客门户 | POST | `/api/customer/portal/appointments/{id}/cancel` | Customer Token |
| 顾客拼团 | GET | `/api/customer/portal/group-buy/templates` | Customer Token |
| 顾客拼团 | GET | `/api/customer/portal/group-buy/orders` | Customer Token |
| 顾客拼团 | POST | `/api/customer/portal/group-buy/orders` | Customer Token |
| 顾客拼团 | POST | `/api/customer/portal/group-buy/orders/{id}/join` | Customer Token |
| 顾客拼团 | GET | `/api/customer/portal/group-buy/my-orders` | Customer Token |

---

## 22. 提成结算模块 `/api/commission/settlements`（F5）

> 2026-05-23 新增。按日期范围聚合消费订单，为技师生成提成结算单。

**状态流转：** `0=草稿` → `1=已确认` → `2=已支付`

### GET `/api/commission/settlements` — 分页查询结算单

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| employeeId | Long | 否 | 按技师筛选 |
| status | Integer | 否 | 按状态筛选：0=草稿 1=已确认 2=已支付 |
| periodStart | String | 否 | 结算周期开始 `yyyy-MM-dd` |
| periodEnd | String | 否 | 结算周期结束 `yyyy-MM-dd` |
| page | int | 否 | 页码（默认 1） |
| pageSize | int | 否 | 每页条数（默认 10） |

**请求示例：** `GET /api/commission/settlements?employeeId=1&status=0&page=1&pageSize=10`

**响应 (200)：**
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "employeeId": 1,
        "employeeName": "刘师傅",
        "periodStart": "2026-05-01",
        "periodEnd": "2026-05-15",
        "orderCount": 25,
        "totalCommission": 1250.00,
        "status": 0,
        "confirmedAt": null,
        "paidAt": null,
        "remark": null,
        "createTime": "2026-05-23 10:00:00"
      }
    ],
    "total": 5,
    "page": 1,
    "pageSize": 10,
    "pages": 1
  }
}
```

### GET `/api/commission/settlements/{id}` — 结算单详情

**路径参数：** `id` — 结算单ID

**响应 (200)：** 返回结算单完整信息，含关联的消费订单明细列表。

### POST `/api/commission/settlements` — 生成结算单

**请求体：**
```json
{
  "periodStart": "2026-05-01",
  "periodEnd": "2026-05-15",
  "employeeId": null
}
```

**字段说明：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| periodStart | String | 是 | 结算周期开始 `yyyy-MM-dd` |
| periodEnd | String | 是 | 结算周期结束 `yyyy-MM-dd` |
| employeeId | Long | 否 | 指定技师ID，不传则生成全部技师的结算单 |

**业务逻辑：** 按日期范围聚合 `consumption_order`（status=1），按 `employee_id` 分组，汇总每个技师的订单数和提成总额，批量生成 status=0 的结算单。

**响应 (200)：** 返回生成的结算单列表。

### PUT `/api/commission/settlements/{id}/confirm` — 确认结算单

**路径参数：** `id` — 结算单ID

**业务逻辑：** 原子状态转换 `0→1`（`WHERE status=0`），记录 `confirmed_at` 时间。

**响应 (200)：** 返回更新后的结算单。
**响应 (400)：** `结算单状态不是草稿，无法确认`

### PUT `/api/commission/settlements/{id}/pay` — 标记已支付

**路径参数：** `id` — 结算单ID

**业务逻辑：** 原子状态转换 `1→2`（`WHERE status=1`），记录 `paid_at` 时间。

**响应 (200)：** 返回更新后的结算单。
**响应 (400)：** `结算单状态不是已确认，无法支付`

### DELETE `/api/commission/settlements/{id}` — 删除结算单

**路径参数：** `id` — 结算单ID

**限制：** 仅可删除 status=0（草稿）的结算单。

**响应 (200)：** `{ "code": 200, "msg": "操作成功", "data": null }`
**响应 (400)：** `只能删除草稿状态的结算单`

---

## 23. 积分商城模块 /api/points（D10）

> 2026-05-23 完成。Admin 端 7 端点 + Customer H5 端 3 端点。

### Admin — 商品管理

### GET `/api/points/products` — 分页查询商品

**查询参数：** `page`(int, 默认1), `size`(int, 默认10), `name`(String, 可选), `status`(Integer, 可选, 1=上架 0=下架)

**响应 (200)：**
```json
{ "code": 200, "data": { "list": [{ "id": 1, "name": "洗发水", "pointsPrice": 100, "stockQty": 50, "exchangedCount": 0, "status": 1, "sortOrder": 0, "createTime": "..." }], "total": 1, "page": 1, "pageSize": 10 } }
```

### GET `/api/points/products/{id}` — 商品详情

**响应 (200)：** 返回 `PointsProductVO`（含 description, originalPrice, updateTime）。

### POST `/api/points/products` — 新增商品

**请求体：**
```json
{ "name": "洗发水", "imageUrl": "", "pointsPrice": 100, "originalPrice": 29.90, "stockQty": 50, "description": "", "status": 1, "sortOrder": 0 }
```

**校验：** `name` @NotBlank, `pointsPrice` @NotNull @Min(1), `stockQty` @NotNull @Min(0)

**响应 (200)：** 返回 `PointsProductVO`。

### PUT `/api/points/products/{id}` — 编辑商品

**请求体：** 同 POST。

**响应 (200)：** 返回更新后的 `PointsProductVO`。**响应 (404)：** 商品不存在。

### PUT `/api/points/products/{id}/status` — 上架/下架

**查询参数：** `status`(Integer, 必填, 1=上架 0=下架)

原子 SQL：`UPDATE points_product SET status = ? WHERE id = ? AND status != ?`

**响应 (200)：** `{ "code": 200, "data": null }`
**响应 (404)：** 商品不存在。

### Admin — 兑换记录

### GET `/api/points/exchange-records` — 分页查询兑换记录

**查询参数：** `page`(int), `size`(int), `memberPhone`(String, 可选), `status`(Integer, 可选, 0=待领取 1=已领取 2=已取消), `startDate`(String, 可选), `endDate`(String, 可选)

**响应 (200)：** `{ "code": 200, "data": { "list": [PointsExchangeRecordVO], "total": N } }`

### PUT `/api/points/exchange-records/{id}/claim` — 标记已领取

原子状态转换 0→1：`UPDATE points_exchange_record SET status = 1, claimed_at = NOW() WHERE id = ? AND status = 0`

**响应 (200)：** 成功。**响应 (400)：** 状态不符（已领取或已取消）。

---

### Customer H5 — 积分商城

> 顾客端端点追加在现有 `CustomerPortalController`（`/api/customer/portal`）。不经过 JWT Filter，使用 `X-Customer-Token` header 认证。

### GET `/api/customer/portal/points/products` — 商品列表

返回 status=1 且 stockQty > 0 的商品，按 sortOrder ASC 排序。

**响应 (200)：** `{ "code": 200, "data": [PointsProductPageVO, ...] }`

### GET `/api/customer/portal/points/products/{id}` — 商品详情

**响应 (200)：** 返回 `PointsProductVO`。**响应 (404)：** 商品不存在。

### POST `/api/customer/portal/points/exchange` — 积分兑换

**请求头：** `X-Customer-Token`（从 token 提取 memberId，保证顾客只能花自己的积分）

**请求体：**
```json
{ "productId": 1, "quantity": 2 }
```

**校验：** `productId` @NotNull, `quantity` @Min(1)

**处理流程（@Transactional）：**
1. 查询 member 获取当前积分
2. 查询 product 校验 status=1 且 stock >= quantity
3. 计算 totalPointsCost = pointsPrice × quantity
4. 校验 member.points >= totalPointsCost
5. 原子扣库存：`UPDATE points_product SET stock_qty = stock_qty - ?, exchanged_count = exchanged_count + ? WHERE id = ? AND stock_qty >= ?`
6. 原子扣积分：`UPDATE member SET points = points - ? WHERE id = ? AND points >= ?`
7. 插入兑换记录（status=0, exchanged_at=NOW()）
8. 每步检查 affected rows == 0

**响应 (200)：** 返回 `PointsExchangeRecordVO`。
**响应 (400)：** `商品库存不足` / `商品已下架` / `会员积分不足`
**响应 (404)：** 商品不存在。

---

## 24. 拼团活动模块 /api/group-buy（D5）

> 2026-05-23 完成。Admin 端 8 端点 + Customer H5 端 7 端点。包含模板管理、团单追踪、参团核销，支持过期自动退款。

### 24.1 Admin — 拼团模板 `/api/group-buy/templates`

#### GET `/api/group-buy/templates` — 分页查询模板

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码（默认 1） |
| size | int | 否 | 每页条数（默认 10） |
| name | String | 否 | 活动名称模糊搜索 |
| status | Integer | 否 | 状态筛选：1=启用 0=停用 |

**响应 (200)：**
```json
{ "code": 200, "data": { "list": [
  { "id": 1, "name": "三人成团·洗剪吹特惠", "groupPrice": 38.00, "originalPrice": 68.00, "groupSize": 3, "expireHours": 24, "issuedQty": 5, "status": 1, "sortOrder": 0 }
], "total": 1, "page": 1, "pageSize": 10 } }
```

#### GET `/api/group-buy/templates/{id}` — 模板详情

**响应 (200)：** 返回 `GroupBuyTemplateVO`（含 id, name, imageUrl, description, originalPrice, groupPrice, groupSize, expireHours, startTime, endTime, totalQty, issuedQty, status, sortOrder, createTime, updateTime）。

#### POST `/api/group-buy/templates` — 新增模板

**请求体：**
```json
{ "name": "三人成团·洗剪吹特惠", "originalPrice": 68.00, "groupPrice": 38.00, "groupSize": 3, "expireHours": 24, "startTime": "2026-05-23 00:00:00", "endTime": "2026-06-30 23:59:59" }
```

**校验：** `name` @NotBlank, `originalPrice` @NotNull, `groupPrice` @NotNull, `groupSize` @NotNull @Min(2)

**响应 (200)：** 返回 `GroupBuyTemplateVO`。

#### PUT `/api/group-buy/templates/{id}` — 编辑模板

**请求体：** 同 POST。仅可编辑未被引用（issuedQty=0）的模板。
**响应 (200)：** 返回更新后的 `GroupBuyTemplateVO`。

#### PUT `/api/group-buy/templates/{id}/status` — 启用/停用

**查询参数：** `status` (Integer, 必填, 1=启用 0=停用)
**响应 (200)：** `{ "code": 200, "data": null }`

### 24.2 Admin — 团单管理 `/api/group-buy`

#### GET `/api/group-buy/orders` — 分页查询团单

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码（默认 1） |
| size | int | 否 | 每页条数（默认 10） |
| templateId | Long | 否 | 按模板筛选 |
| status | Integer | 否 | 状态：1=拼团中 2=已成团 3=已核销 4=已过期 5=已取消 |
| startDate | String | 否 | 开始日期 yyyy-MM-dd |
| endDate | String | 否 | 结束日期 yyyy-MM-dd |

**响应 (200)：**
```json
{ "code": 200, "data": { "list": [
  { "id": 1, "orderNo": "GB20260523A001", "templateName": "洗剪吹特惠", "leaderName": "张三", "groupPrice": 38.00, "groupSize": 3, "currentSize": 2, "expireTime": "2026-05-24 15:00:00", "status": 1 }
], "total": 5, "page": 1, "pageSize": 10 } }
```

#### GET `/api/group-buy/orders/{id}` — 团单详情（含参团列表）

**响应 (200)：** 返回 `GroupBuyOrderVO`（含团单信息 + participants 数组，每个参与者含 memberId, memberName, memberPhone, joinPrice, status, isLeader, joinTime）。

#### PUT `/api/group-buy/participants/{id}/redeem` — 核销参团记录

**业务逻辑：** 原子状态转换 `2→3`（已成团→已核销）：`UPDATE group_buy_participant SET status = 3 WHERE id = ? AND status = 2`。

**响应 (200)：** 成功。**响应 (400)：** `状态不符，无法核销`

### 24.3 Customer H5 — 拼团活动

> 顾客端端点追加在现有 `CustomerPortalController`（`/api/customer/portal`）。使用 `X-Customer-Token` header 认证。

#### GET `/api/customer/portal/group-buy/templates` — 拼团活动列表

返回 status=1 的启用模板，最多 100 条。
**响应 (200)：** `{ "code": 200, "data": [GroupBuyTemplatePageVO, ...] }`

#### GET `/api/customer/portal/group-buy/templates/{id}` — 活动详情

**响应 (200)：** 返回 `GroupBuyTemplateVO`。**响应 (404)：** 模板不存在。

#### GET `/api/customer/portal/group-buy/orders` — 可参团列表

返回 status=1（拼团中）且 currentSize < groupSize 的团单。
**响应 (200)：** `{ "code": 200, "data": [GroupBuyOrderPageVO, ...] }`

#### GET `/api/customer/portal/group-buy/orders/{id}` — 团单详情

**响应 (200)：** 返回 `GroupBuyOrderVO`（含参与者列表）。

#### POST `/api/customer/portal/group-buy/orders` — 开团

**请求头：** `X-Customer-Token`（必填，自动提取 memberId）
**请求体：**
```json
{ "templateId": 1 }
```

**处理流程（@Transactional）：**
1. 校验模板 status=1
2. 校验会员余额 >= groupPrice
3. 原子扣款：`UPDATE member SET balance = balance - ? WHERE id = ? AND balance >= ?`
4. 创建 group_buy_order（orderNo 格式：GB-yyyyMMdd-xxxxx，expireTime = now + expireHours）
5. 创建团长 participant（isLeader=1, joinPrice=groupPrice）
6. 更新 template.issuedQty + 1

**响应 (200)：** 返回 `GroupBuyOrderVO`。
**响应 (400)：** `余额不足` / `模板已停用`

#### POST `/api/customer/portal/group-buy/orders/{id}/join` — 参团

**请求头：** `X-Customer-Token`（必填）
**路径参数：** `id` — 团单ID

**处理流程（@Transactional）：**
1. 校验团单 status=1 且 currentSize < groupSize
2. 校验会员未重复参团（uk_order_member 去重）
3. 原子扣款：`UPDATE member SET balance = balance - ? WHERE id = ? AND balance >= ?`
4. 插入 participant（joinPrice=groupPrice, status=1）
5. 原子增加 currentSize：`UPDATE group_buy_order SET current_size = current_size + 1 WHERE id = ?`
6. 若 currentSize 达标 → 自动成团：UPDATE order status=2 + 批量 UPDATE participants status=2

**响应 (200)：** 返回 `GroupBuyOrderVO`。
**响应 (400)：** `该团单已满员` / `您已参与该团单` / `余额不足`

#### GET `/api/customer/portal/group-buy/my-orders` — 我的拼团

**请求头：** `X-Customer-Token`（必填）
**查询参数：** `page`（默认1）、`size`（默认10）

**响应 (200)：** 分页返回 `GroupBuyOrderPageVO`（按 createTime 倒序）。

### 24.4 拼团活动 API 路径速查

| 模块 | 方法 | 完整路径 | 认证 |
|------|------|----------|------|
| 拼团模板 | GET | `/api/group-buy/templates` | 是 |
| 拼团模板 | GET | `/api/group-buy/templates/{id}` | 是 |
| 拼团模板 | POST | `/api/group-buy/templates` | 是 |
| 拼团模板 | PUT | `/api/group-buy/templates/{id}` | 是 |
| 拼团模板 | PUT | `/api/group-buy/templates/{id}/status` | 是 |
| 拼团团单 | GET | `/api/group-buy/orders` | 是 |
| 拼团团单 | GET | `/api/group-buy/orders/{id}` | 是 |
| 拼团团单 | PUT | `/api/group-buy/participants/{id}/redeem` | 是 |
| 顾客拼团 | GET | `/api/customer/portal/group-buy/templates` | Customer Token |
| 顾客拼团 | GET | `/api/customer/portal/group-buy/templates/{id}` | Customer Token |
| 顾客拼团 | GET | `/api/customer/portal/group-buy/orders` | Customer Token |
| 顾客拼团 | GET | `/api/customer/portal/group-buy/orders/{id}` | Customer Token |
| 顾客拼团 | POST | `/api/customer/portal/group-buy/orders` | Customer Token |
| 顾客拼团 | POST | `/api/customer/portal/group-buy/orders/{id}/join` | Customer Token |
| 顾客拼团 | GET | `/api/customer/portal/group-buy/my-orders` | Customer Token |

---

## 25. 生日营销配置模块 `/api/birthday-config`（2026-05-24 新增）

> 管理端配置生日礼券自动发放规则，`BirthdayScheduler` 每天 8:00 从此配置读取规则替代原先硬编码查找"生日礼券"模板。

### GET `/api/birthday-config` — 获取当前配置

**认证：** 是（管理员/经理）

**响应 (200)：**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "enabled": 1,
    "couponTemplateId": 3,
    "smsEnabled": 1,
    "updateTime": "2026-05-24 15:00:00"
  }
}
```

### PUT `/api/birthday-config` — 更新配置

**认证：** 是（管理员/经理）
**请求体：**
```json
{
  "enabled": 1,
  "couponTemplateId": 3,
  "smsEnabled": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| enabled | Integer | 否 | 1=启用 0=停用 |
| couponTemplateId | Long | 否 | 优惠券模板 ID |
| smsEnabled | Integer | 否 | 1=发短信 0=不发 |

**响应 (200)：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "enabled": 1,
    "couponTemplateId": 3,
    "smsEnabled": 1,
    "updateTime": "2026-05-24 15:00:00"
  }
}
```

### 生日营销 API 路径速查

| 模块 | 方法 | 完整路径 | 认证 |
|------|------|----------|------|
| 生日配置 | GET | `/api/birthday-config` | 是 |
| 生日配置 | PUT | `/api/birthday-config` | 是 |

---

## 27. 菜单配置模块 `/api/menu-config`

### 27.1 获取菜单分组配置

**GET** `/api/menu-config`

**响应 (200)：**
```json
{
  "code": 200,
  "data": [
    { "menuIndex": "/member", "groupName": "会员管理" },
    { "menuIndex": "/service", "groupName": "系统设置" }
  ]
}
```

### 27.2 保存菜单分组配置（全量替换）

**PUT** `/api/menu-config`

**请求体：**
```json
[
  { "menuIndex": "/member", "groupName": "会员管理" },
  { "menuIndex": "/service", "groupName": "系统设置" }
]
```

**响应 (200)：** `{ "code": 200, "msg": "操作成功" }`

### 菜单配置 API 路径速查

| 模块 | 方法 | 完整路径 | 认证 |
|------|------|----------|------|
| 菜单配置 | GET | `/api/menu-config` | 是 |
| 菜单配置 | PUT | `/api/menu-config` | 是 |

---

## 28. 仪表盘快捷入口模块 `/api/dashboard/quick-actions`

### 28.1 获取快捷入口配置

**GET** `/api/dashboard/quick-actions`

**响应 (200)：**
```json
{
  "code": 200,
  "data": [
    { "slot": 0, "label": "消费收银", "path": "/consumption" },
    { "slot": 1, "label": "新增预约", "path": "/appointment" }
  ]
}
```

### 28.2 保存快捷入口配置（全量替换）

**PUT** `/api/dashboard/quick-actions`

**请求体：**
```json
{
  "items": [
    { "slot": 0, "label": "消费收银", "path": "/consumption" },
    { "slot": 1, "label": "新增预约", "path": "/appointment" }
  ]
}
```

**响应 (200)：** `{ "code": 200, "msg": "操作成功" }`

### 快捷入口 API 路径速查

| 模块 | 方法 | 完整路径 | 认证 |
|------|------|----------|------|
| 快捷入口 | GET | `/api/dashboard/quick-actions` | 是 |
| 快捷入口 | PUT | `/api/dashboard/quick-actions` | 是 |
