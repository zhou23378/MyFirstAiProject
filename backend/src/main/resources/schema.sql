-- ============================================================
-- 美发沙龙管理系统 - 数据库初始化脚本
-- 来源：mysqldump 实库结构同步 (2026-05-21)
-- 用途：新环境首次部署时执行（Docker / 新电脑）
-- 导入：mysql -u root -p salon < schema.sql
-- ============================================================

-- 管理员表
CREATE TABLE IF NOT EXISTS `admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `role` varchar(20) DEFAULT 'ADMIN' COMMENT '角色：ADMIN/manager/technician/cashier',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 会员表
CREATE TABLE IF NOT EXISTS `member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '会员姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `gender` tinyint DEFAULT '0' COMMENT '性别：0=未知 1=男 2=女',
  `level` int DEFAULT '0' COMMENT '会员等级',
  `points` int DEFAULT '0' COMMENT '积分',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '余额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `tags` varchar(255) DEFAULT NULL COMMENT '会员标签，逗号分隔',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `birthday_type` tinyint DEFAULT '1' COMMENT '生日类型：1=公历 2=农历',
  `last_consume_date` date DEFAULT NULL COMMENT '最后消费日期',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `rfm_segment` varchar(20) DEFAULT NULL COMMENT 'RFM分层',
  `last_visit_date` date DEFAULT NULL COMMENT '最后到店日期',
  `total_spent` decimal(10,2) DEFAULT '0.00' COMMENT '累计消费金额',
  `visit_count` int DEFAULT '0' COMMENT '到店次数',
  `password` varchar(255) DEFAULT NULL COMMENT '顾客登录密码(BCrypt)',
  `openid` varchar(64) DEFAULT NULL COMMENT '微信OpenID',
  PRIMARY KEY (`id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- 服务分类表
CREATE TABLE IF NOT EXISTS `service_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `sort` int DEFAULT '0' COMMENT '排序号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务分类表';

-- 服务项目表
CREATE TABLE IF NOT EXISTS `service_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` bigint NOT NULL COMMENT '所属分类ID',
  `name` varchar(100) NOT NULL COMMENT '项目名称',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '价格',
  `duration` int DEFAULT '0' COMMENT '时长（分钟）',
  `status` tinyint DEFAULT '1' COMMENT '状态：0=下架 1=上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `commission_type` tinyint DEFAULT '0' COMMENT '提成类型：0=无 1=固定金额 2=比例',
  `commission_value` decimal(10,2) DEFAULT '0.00' COMMENT '提成值（金额或比例）',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目表';

-- 消费订单表
CREATE TABLE IF NOT EXISTS `consumption_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `pay_method` tinyint DEFAULT '1' COMMENT '支付方式：1=现金 2=余额 3=微信 4=支付宝 5=银行卡 6=储值卡 7=团购券 8=混合',
  `pay_amount` decimal(10,2) DEFAULT '0.00' COMMENT '支付金额（现金/微信部分）',
  `balance_used` decimal(10,2) DEFAULT '0.00' COMMENT '使用余额',
  `points_earned` int DEFAULT '0' COMMENT '获得积分',
  `status` tinyint DEFAULT '1' COMMENT '订单状态：1=正常 2=已退款',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `pay_remark` varchar(255) DEFAULT NULL COMMENT '支付备注',
  `coupon_id` bigint DEFAULT NULL COMMENT '使用优惠券ID',
  `coupon_discount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠券优惠金额',
  `employee_id` bigint DEFAULT NULL COMMENT '服务技师ID',
  `commission_amount` decimal(10,2) DEFAULT '0.00' COMMENT '提成金额',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费订单表';

-- 消费订单明细表
CREATE TABLE IF NOT EXISTS `consumption_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `item_id` bigint NOT NULL COMMENT '服务项目ID',
  `item_name` varchar(100) NOT NULL COMMENT '项目名称',
  `item_price` decimal(10,2) NOT NULL COMMENT '项目价格',
  `quantity` int DEFAULT '1' COMMENT '数量',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费订单明细表';

-- 会员等级表
CREATE TABLE IF NOT EXISTS `member_level` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '等级名称',
  `points_required` int DEFAULT '0' COMMENT '所需积分门槛',
  `discount_rate` decimal(3,2) DEFAULT '1.00' COMMENT '折扣率',
  `sort` int DEFAULT '0' COMMENT '排序号',
  `status` tinyint DEFAULT '1' COMMENT '状态：1=启用 0=停用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

-- 员工表
CREATE TABLE IF NOT EXISTS `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '员工姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `position` varchar(50) DEFAULT NULL COMMENT '职位',
  `salary` decimal(10,2) DEFAULT '0.00' COMMENT '薪资',
  `status` tinyint DEFAULT '1' COMMENT '状态：0=离职 1=在职',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- 预约表
CREATE TABLE IF NOT EXISTS `appointment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `service_item_id` bigint DEFAULT NULL COMMENT '服务项目ID',
  `employee_id` bigint DEFAULT NULL COMMENT '技师ID',
  `appointment_date` date NOT NULL COMMENT '预约日期',
  `start_time` time DEFAULT NULL COMMENT '开始时间',
  `end_time` time DEFAULT NULL COMMENT '结束时间',
  `duration` int DEFAULT NULL COMMENT '时长（分钟）',
  `status` tinyint DEFAULT '1' COMMENT '状态：1=已预约 2=已到店 3=已完成 4=已取消 5=爽约',
  `reminded` tinyint DEFAULT 0 COMMENT '是否已发送提醒：0=未发送 1=已发送',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_date` (`appointment_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- 支付明细表（组合支付场景）
CREATE TABLE IF NOT EXISTS `payment_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `pay_method` tinyint NOT NULL COMMENT '支付方式',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `transaction_id` varchar(64) DEFAULT NULL COMMENT '第三方交易号',
  `pay_channel` varchar(20) DEFAULT NULL COMMENT '支付渠道：WECHAT/ALIPAY/CASH/BALANCE',
  `qr_code_url` varchar(500) DEFAULT NULL COMMENT '支付二维码链接',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付明细表';

-- 充值记录表
CREATE TABLE IF NOT EXISTS `recharge_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `amount` decimal(10,2) NOT NULL COMMENT '充值金额',
  `pay_method` int NOT NULL COMMENT '支付方式：1=现金 2=微信 3=支付宝 4=银行卡',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

-- ============================================================
-- Phase 2: 库存管理
-- ============================================================

CREATE TABLE IF NOT EXISTS `product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE IF NOT EXISTS `supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '供应商名称',
  `contact` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` bigint DEFAULT NULL COMMENT '商品分类ID',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `unit` varchar(20) DEFAULT '瓶' COMMENT '单位（瓶/盒/支/包）',
  `sale_price` decimal(10,2) DEFAULT '0.00' COMMENT '建议售价',
  `stock_qty` int DEFAULT '0' COMMENT '当前库存量',
  `alert_qty` int DEFAULT '10' COMMENT '库存预警阈值',
  `status` tinyint DEFAULT '1' COMMENT '状态：1=在售 0=停售',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE IF NOT EXISTS `stock_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `type` tinyint NOT NULL COMMENT '类型：1=入库 2=出库 3=退货',
  `qty` int NOT NULL COMMENT '数量',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '单价',
  `total_amount` decimal(10,2) DEFAULT '0.00' COMMENT '总金额',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID（入库时关联）',
  `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- ============================================================
-- Phase 3: 优惠券模块
-- ============================================================

CREATE TABLE IF NOT EXISTS `coupon_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '券名称',
  `type` tinyint NOT NULL COMMENT '类型：1=满减券 2=折扣券 3=代金券',
  `condition_amount` decimal(10,2) DEFAULT '0.00' COMMENT '满减门槛金额（满减券必填）',
  `discount_value` decimal(10,2) NOT NULL COMMENT '优惠值（满减=减N元，折扣=N折如0.85）',
  `valid_days` int DEFAULT '30' COMMENT '有效天数',
  `total_qty` int DEFAULT '0' COMMENT '发行总量（0=不限量）',
  `issued_qty` int DEFAULT '0' COMMENT '已发放数量',
  `status` tinyint DEFAULT '1' COMMENT '状态：1=启用 0=停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

CREATE TABLE IF NOT EXISTS `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` bigint NOT NULL COMMENT '券模板ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `code` varchar(20) NOT NULL COMMENT '券码',
  `status` tinyint DEFAULT '1' COMMENT '状态：1=未使用 2=已使用 3=已过期',
  `discount_value` decimal(10,2) NOT NULL COMMENT '快照优惠值',
  `condition_amount` decimal(10,2) DEFAULT '0.00' COMMENT '快照门槛',
  `receive_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `order_id` bigint DEFAULT NULL COMMENT '核销订单ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_member` (`member_id`),
  KEY `idx_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- ============================================================
-- Phase 4: 排班考勤 + 次卡
-- ============================================================

CREATE TABLE IF NOT EXISTS `shift_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '班次名称',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `color` varchar(20) DEFAULT '#409EFF' COMMENT '显示颜色',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次模板表';

CREATE TABLE IF NOT EXISTS `schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `shift_id` bigint NOT NULL COMMENT '班次模板ID',
  `date` date NOT NULL COMMENT '排班日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_date` (`employee_id`,`date`),
  KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工排班表';

CREATE TABLE IF NOT EXISTS `attendance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `schedule_id` bigint DEFAULT NULL COMMENT '关联排班ID',
  `date` date NOT NULL COMMENT '考勤日期',
  `clock_in` datetime DEFAULT NULL COMMENT '上班打卡时间',
  `clock_out` datetime DEFAULT NULL COMMENT '下班打卡时间',
  `status` tinyint DEFAULT '1' COMMENT '状态：1=正常 2=迟到 3=早退 4=缺勤',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_emp_date` (`employee_id`,`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤打卡表';

CREATE TABLE IF NOT EXISTS `service_card` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `service_item_id` bigint NOT NULL COMMENT '服务项目ID',
  `name` varchar(100) NOT NULL COMMENT '次卡名称',
  `total_count` int NOT NULL COMMENT '总次数',
  `used_count` int DEFAULT '0' COMMENT '已用次数',
  `price` decimal(10,2) NOT NULL COMMENT '购买价格',
  `status` tinyint DEFAULT '1' COMMENT '1=有效 2=用完 3=过期 4=退款',
  `expire_date` date DEFAULT NULL COMMENT '过期日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='次卡表';

-- ============================================================
-- Phase 5: 审计日志
-- ============================================================

CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) DEFAULT NULL COMMENT '操作人用户名',
  `role` varchar(20) DEFAULT NULL COMMENT '操作人角色',
  `action` varchar(200) NOT NULL COMMENT '操作描述',
  `method` varchar(10) DEFAULT NULL COMMENT 'HTTP方法',
  `url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `ip` varchar(50) DEFAULT NULL COMMENT '客户端IP',
  `params` text COMMENT '请求参数（脱敏后JSON）',
  `status` varchar(20) DEFAULT NULL COMMENT '执行状态：SUCCESS/FAILED',
  `error_message` varchar(2000) DEFAULT NULL COMMENT '错误信息',
  `cost_time` bigint DEFAULT NULL COMMENT '耗时（毫秒）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- ============================================================
-- Phase 7: 店务管理（技师状态/排队/计时/日结）
-- ============================================================

CREATE TABLE IF NOT EXISTS `technician_status` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `status` varchar(20) NOT NULL DEFAULT 'OFF_DUTY' COMMENT 'AVAILABLE|BUSY|BREAK|OFF_DUTY',
  `current_customer_name` varchar(50) DEFAULT NULL COMMENT '当前服务客人',
  `current_service_name` varchar(100) DEFAULT NULL COMMENT '当前服务项目',
  `last_status_changed` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '状态变更时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `employee_id` (`employee_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技师实时状态表';

CREATE TABLE IF NOT EXISTS `service_queue` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `member_name` varchar(50) NOT NULL COMMENT '会员姓名',
  `service_item_id` bigint DEFAULT NULL COMMENT '期望服务项目ID',
  `service_item_name` varchar(100) DEFAULT NULL COMMENT '期望服务项目名',
  `queue_number` int NOT NULL COMMENT '排队号',
  `status` tinyint DEFAULT '1' COMMENT '1=等待中 2=已分配 3=已取消 4=已跳过',
  `assigned_employee_id` bigint DEFAULT NULL COMMENT '分配的技师ID',
  `wait_since` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '开始等待时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_queue_number` (`queue_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮牌排队表';

CREATE TABLE IF NOT EXISTS `service_timer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `appointment_id` bigint DEFAULT NULL COMMENT '关联预约ID',
  `queue_id` bigint DEFAULT NULL COMMENT '关联排队ID',
  `employee_id` bigint NOT NULL COMMENT '技师ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `service_item_name` varchar(100) NOT NULL COMMENT '服务项目名',
  `service_item_id` bigint DEFAULT NULL COMMENT '服务项目ID',
  `planned_duration` int NOT NULL COMMENT '计划时长（分钟）',
  `actual_duration` int DEFAULT NULL COMMENT '实际时长（分钟）',
  `status` tinyint DEFAULT '1' COMMENT '1=进行中 2=已暂停 3=已完成',
  `started_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `paused_at` datetime DEFAULT NULL COMMENT '暂停时间',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  `alert_80_sent` tinyint DEFAULT '0' COMMENT '80%提醒已发',
  `alert_100_sent` tinyint DEFAULT '0' COMMENT '100%提醒已发',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务计时表';

CREATE TABLE IF NOT EXISTS `daily_close` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `close_date` date NOT NULL COMMENT '日结日期',
  `system_cash` decimal(10,2) DEFAULT '0.00' COMMENT '系统现金',
  `system_wechat` decimal(10,2) DEFAULT '0.00' COMMENT '系统微信',
  `system_alipay` decimal(10,2) DEFAULT '0.00' COMMENT '系统支付宝',
  `system_balance` decimal(10,2) DEFAULT '0.00' COMMENT '系统余额支付',
  `system_card` decimal(10,2) DEFAULT '0.00' COMMENT '系统储值卡',
  `system_total` decimal(10,2) DEFAULT '0.00' COMMENT '系统合计',
  `manual_cash` decimal(10,2) DEFAULT NULL COMMENT '人工现金',
  `manual_wechat` decimal(10,2) DEFAULT NULL COMMENT '人工微信',
  `manual_alipay` decimal(10,2) DEFAULT NULL COMMENT '人工支付宝',
  `manual_total` decimal(10,2) DEFAULT NULL COMMENT '人工合计',
  `diff_amount` decimal(10,2) DEFAULT '0.00' COMMENT '差异金额',
  `status` tinyint DEFAULT '0' COMMENT '0=草稿 1=已锁定',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `locked_by` varchar(50) DEFAULT NULL COMMENT '锁定人',
  `locked_at` datetime DEFAULT NULL COMMENT '锁定时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `close_date` (`close_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务日结表';

-- ============================================================
-- Phase 7b: 营销（通知日志/标签规则/会员标签）
-- ============================================================

CREATE TABLE IF NOT EXISTS `notification_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` bigint DEFAULT NULL COMMENT '会员ID',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `type` varchar(30) NOT NULL COMMENT '类型：BIRTHDAY|APPOINTMENT_REMINDER|PAYMENT|RECHARGE|MARKETING',
  `template_code` varchar(50) DEFAULT NULL COMMENT '短信模板ID',
  `content` varchar(500) DEFAULT NULL COMMENT '短信内容',
  `status` tinyint DEFAULT '0' COMMENT '0=发送中 1=成功 2=失败',
  `error_msg` varchar(255) DEFAULT NULL COMMENT '失败原因',
  `sent_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `is_read` tinyint DEFAULT 0 COMMENT '0=未读 1=已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知日志表';

CREATE TABLE IF NOT EXISTS `birthday_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enabled` tinyint DEFAULT 1 COMMENT '0=停用 1=启用',
  `coupon_template_id` bigint DEFAULT NULL COMMENT '发放的优惠券模板ID',
  `sms_enabled` tinyint DEFAULT 1 COMMENT '0=不发短信 1=发送短信',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生日营销配置表';

INSERT IGNORE INTO `birthday_config` (`id`, `enabled`, `coupon_template_id`, `sms_enabled`) VALUES (1, 1, NULL, 1);

CREATE TABLE IF NOT EXISTS `tag_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '规则名称',
  `tag_name` varchar(30) NOT NULL COMMENT '生成的标签名',
  `conditions_json` text NOT NULL COMMENT '条件JSON',
  `enabled` tinyint DEFAULT '1' COMMENT '0=禁用 1=启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自动标签规则表';

CREATE TABLE IF NOT EXISTS `member_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `tag_name` varchar(30) NOT NULL COMMENT '标签名',
  `source` varchar(20) DEFAULT 'AUTO' COMMENT '来源：AUTO|MANUAL',
  `rule_id` bigint DEFAULT NULL COMMENT '来源规则ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_tag` (`member_id`,`tag_name`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员标签表';

-- ============================================================
-- Phase 8: 顾客端（会话/支付）
-- ============================================================

CREATE TABLE IF NOT EXISTS `customer_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `token` varchar(128) NOT NULL COMMENT '会话Token',
  `login_method` varchar(20) NOT NULL COMMENT '登录方式：SMS_CODE|PASSWORD|WECHAT',
  `expire_at` datetime NOT NULL COMMENT '过期时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `idx_token` (`token`),
  KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾客登录会话表';

-- ============================================================
-- 种子数据（INSERT IGNORE 确保幂等）
-- ============================================================

-- 默认管理员账号：admin / admin123（BCrypt 加密）
INSERT IGNORE INTO admin (username, password) VALUES ('admin', '$2b$10$txrlKTe2OIyKg2nK2PLP/.y37EL65r3Tn1F6ThLdsE2WdziNAfH/m');

-- 默认服务分类
INSERT IGNORE INTO service_category (id, name, sort) VALUES
(1, '剪发', 1),
(2, '染发', 2),
(3, '护理', 3);

-- 默认服务项目
INSERT IGNORE INTO service_item (id, category_id, name, price, duration, status) VALUES
(1, 1, '洗剪吹', 68.00, 30, 1),
(2, 1, '精剪', 88.00, 45, 1),
(3, 2, '全染', 268.00, 90, 1),
(4, 2, '挑染', 198.00, 60, 1),
(5, 3, '基础护理', 128.00, 45, 1),
(6, 3, '深层护理', 198.00, 60, 1);

-- 默认会员等级
INSERT IGNORE INTO member_level (id, name, points_required, discount_rate, sort, status) VALUES
(1, '普通会员', 0, 1.00, 1, 1),
(2, '银卡会员', 200, 0.95, 2, 1),
(3, '金卡会员', 500, 0.90, 3, 1),
(4, '钻石会员', 1000, 0.85, 4, 1);

-- 默认商品分类
INSERT IGNORE INTO product_category (id, name, sort) VALUES
(1, '染膏', 1),
(2, '洗发水', 2),
(3, '护发产品', 3),
(4, '造型产品', 4);

-- 默认优惠券模板
INSERT IGNORE INTO coupon_template (id, name, type, condition_amount, discount_value, valid_days, total_qty, status, remark) VALUES
(1, '新客满减券', 1, 100.00, 20.00, 30, 100, 1, '新会员注册赠送'),
(2, '全场85折券', 2, 0.00, 0.85, 60, 50, 1, '限时活动'),
(3, '10元代金券', 3, 0.00, 10.00, 90, 200, 1, '会员生日赠送');

-- 默认班次模板
INSERT IGNORE INTO shift_template (id, name, start_time, end_time, color, sort) VALUES
(1, '早班', '09:00:00', '17:00:00', '#409EFF', 1),
(2, '晚班', '13:00:00', '21:00:00', '#E6A23C', 2),
(3, '全天', '09:00:00', '21:00:00', '#67C23A', 3);

-- ============================================================
-- 审计修复（2026-05-22）: 补齐缺失时间戳字段 + 性能索引
-- ============================================================

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'consumption_order_item' AND column_name = 'create_time'
  ),
  'SELECT 1',
  'ALTER TABLE consumption_order_item ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'consumption_order_item' AND column_name = 'update_time'
  ),
  'SELECT 1',
  'ALTER TABLE consumption_order_item ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'payment_detail' AND column_name = 'update_time'
  ),
  'SELECT 1',
  'ALTER TABLE payment_detail ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'recharge_record' AND column_name = 'update_time'
  ),
  'SELECT 1',
  'ALTER TABLE recharge_record ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'audit_log' AND column_name = 'update_time'
  ),
  'SELECT 1',
  'ALTER TABLE audit_log ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'customer_session' AND column_name = 'update_time'
  ),
  'SELECT 1',
  'ALTER TABLE customer_session ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'employee' AND index_name = 'idx_employee_phone'
  ),
  'SELECT 1',
  'CREATE INDEX idx_employee_phone ON employee(phone)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'employee' AND index_name = 'idx_employee_status'
  ),
  'SELECT 1',
  'CREATE INDEX idx_employee_status ON employee(status)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'consumption_order' AND index_name = 'idx_consumption_order_status'
  ),
  'SELECT 1',
  'CREATE INDEX idx_consumption_order_status ON consumption_order(status)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'consumption_order' AND index_name = 'idx_consumption_order_create_time'
  ),
  'SELECT 1',
  'CREATE INDEX idx_consumption_order_create_time ON consumption_order(create_time)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'audit_log' AND index_name = 'idx_audit_log_username'
  ),
  'SELECT 1',
  'CREATE INDEX idx_audit_log_username ON audit_log(username)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'audit_log' AND index_name = 'idx_audit_log_create_time'
  ),
  'SELECT 1',
  'CREATE INDEX idx_audit_log_create_time ON audit_log(create_time)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'customer_session' AND index_name = 'idx_customer_session_expire_at'
  ),
  'SELECT 1',
  'CREATE INDEX idx_customer_session_expire_at ON customer_session(expire_at)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- Phase 10: 提成结算（F5）
-- ============================================================

CREATE TABLE IF NOT EXISTS `commission_settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `employee_name` varchar(50) NOT NULL COMMENT '员工姓名',
  `period_start` date NOT NULL COMMENT '结算周期起始',
  `period_end` date NOT NULL COMMENT '结算周期截止',
  `order_count` int DEFAULT 0 COMMENT '服务订单数',
  `total_commission` decimal(12,2) DEFAULT '0.00' COMMENT '提成总额',
  `status` tinyint DEFAULT 0 COMMENT '0=草稿 1=已确认 2=已支付',
  `confirmed_at` datetime DEFAULT NULL COMMENT '确认时间',
  `paid_at` datetime DEFAULT NULL COMMENT '支付时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_employee` (`employee_id`),
  KEY `idx_period` (`period_start`, `period_end`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提成结算表';

-- ============================================================
-- Phase 9: 采购订单 + 退货管理（G7 + G8）
-- ============================================================

CREATE TABLE IF NOT EXISTS `purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单编号',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID',
  `status` tinyint DEFAULT 0 COMMENT '0=草稿 1=已提交 2=已审批 3=已收货 4=已取消',
  `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '采购总金额',
  `item_count` int DEFAULT 0 COMMENT '商品种类数',
  `total_qty` int DEFAULT 0 COMMENT '商品总数量',
  `applicant` varchar(50) DEFAULT NULL COMMENT '申请人',
  `approver` varchar(50) DEFAULT NULL COMMENT '审批人',
  `approved_time` datetime DEFAULT NULL COMMENT '审批时间',
  `received_time` datetime DEFAULT NULL COMMENT '收货时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_supplier` (`supplier_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

CREATE TABLE IF NOT EXISTS `purchase_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '采购订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `qty` int NOT NULL COMMENT '采购数量',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '采购单价',
  `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '小计金额',
  `received_qty` int DEFAULT 0 COMMENT '已收数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细表';

CREATE TABLE IF NOT EXISTS `return_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '退货单号',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID',
  `original_order_id` bigint DEFAULT NULL COMMENT '关联采购订单ID',
  `status` tinyint DEFAULT 0 COMMENT '0=草稿 1=已提交 2=已审批 3=已完成 4=已驳回',
  `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '退货总金额',
  `item_count` int DEFAULT 0 COMMENT '商品种类数',
  `total_qty` int DEFAULT 0 COMMENT '退货总数量',
  `reason` varchar(500) DEFAULT NULL COMMENT '退货原因',
  `applicant` varchar(50) DEFAULT NULL COMMENT '申请人',
  `approver` varchar(50) DEFAULT NULL COMMENT '审批人',
  `approved_time` datetime DEFAULT NULL COMMENT '审批时间',
  `completed_time` datetime DEFAULT NULL COMMENT '完成时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_supplier` (`supplier_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货订单表';

CREATE TABLE IF NOT EXISTS `return_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '退货订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `qty` int NOT NULL COMMENT '退货数量',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '退货单价',
  `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '小计金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货订单明细表';

-- ──────────── 积分商城 ────────────

CREATE TABLE IF NOT EXISTS `points_product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `image_url` varchar(500) DEFAULT NULL COMMENT '商品图片',
  `points_price` int NOT NULL COMMENT '兑换所需积分',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价（展示用）',
  `stock_qty` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `exchanged_count` int DEFAULT 0 COMMENT '已兑换数量',
  `description` text COMMENT '商品描述',
  `status` tinyint DEFAULT 1 COMMENT '0=下架 1=上架',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商品表';

CREATE TABLE IF NOT EXISTS `points_exchange_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `member_name` varchar(50) DEFAULT NULL COMMENT '会员姓名',
  `member_phone` varchar(20) DEFAULT NULL COMMENT '会员手机号',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(100) DEFAULT NULL COMMENT '商品名称',
  `points_cost` int NOT NULL COMMENT '消耗积分',
  `quantity` int DEFAULT 1 COMMENT '兑换数量',
  `status` tinyint DEFAULT 0 COMMENT '0=待领取 1=已领取 2=已取消',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID（后台兑换时）',
  `exchanged_at` datetime DEFAULT NULL COMMENT '兑换时间',
  `claimed_at` datetime DEFAULT NULL COMMENT '领取时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_member` (`member_id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换记录表';

-- 拼团活动 D5
CREATE TABLE IF NOT EXISTS `group_buy_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '活动名称',
  `image_url` varchar(500) DEFAULT NULL COMMENT '活动图片',
  `description` text COMMENT '活动说明',
  `original_price` decimal(10,2) NOT NULL COMMENT '原价',
  `group_price` decimal(10,2) NOT NULL COMMENT '拼团价',
  `group_size` int NOT NULL COMMENT '成团人数（含团长）',
  `expire_hours` int NOT NULL DEFAULT 24 COMMENT '拼团有效时长（小时）',
  `start_time` datetime DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '活动结束时间',
  `total_qty` int DEFAULT 0 COMMENT '发行总量（0=不限）',
  `issued_qty` int DEFAULT 0 COMMENT '已开团数量',
  `status` tinyint DEFAULT 1 COMMENT '0=停用 1=启用',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort_order`),
  KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团模板表';

CREATE TABLE IF NOT EXISTS `group_buy_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_id` bigint NOT NULL COMMENT '拼团模板ID',
  `order_no` varchar(32) NOT NULL COMMENT '团单号',
  `leader_id` bigint NOT NULL COMMENT '团长会员ID',
  `leader_name` varchar(50) DEFAULT NULL COMMENT '团长姓名',
  `leader_phone` varchar(20) DEFAULT NULL COMMENT '团长手机号',
  `group_price` decimal(10,2) NOT NULL COMMENT '拼团价（快照）',
  `original_price` decimal(10,2) NOT NULL COMMENT '原价（快照）',
  `group_size` int NOT NULL COMMENT '成团人数（快照）',
  `current_size` int NOT NULL DEFAULT 1 COMMENT '当前参与人数',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `status` tinyint DEFAULT 1 COMMENT '1=拼团中 2=已成团 3=已核销 4=已过期 5=已取消',
  `complete_time` datetime DEFAULT NULL COMMENT '成团时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_template` (`template_id`),
  KEY `idx_leader` (`leader_id`),
  KEY `idx_status_expire` (`status`, `expire_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团团单表';

CREATE TABLE IF NOT EXISTS `group_buy_participant` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '团单ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `member_name` varchar(50) DEFAULT NULL COMMENT '会员姓名',
  `member_phone` varchar(20) DEFAULT NULL COMMENT '会员手机号',
  `join_price` decimal(10,2) NOT NULL COMMENT '参与价格（快照）',
  `status` tinyint DEFAULT 1 COMMENT '1=待成团 2=已成团 3=已核销 4=已退款',
  `is_leader` tinyint DEFAULT 0 COMMENT '0=团员 1=团长',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '参与时间',
  `complete_time` datetime DEFAULT NULL COMMENT '成团时间',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_member` (`order_id`, `member_id`),
  KEY `idx_member` (`member_id`),
  KEY `idx_status` (`status`),
  KEY `idx_join_time` (`join_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团参与记录表';
-- 服务评价表
CREATE TABLE IF NOT EXISTS `service_rating` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '消费订单ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `rating` tinyint NOT NULL COMMENT '评分 1-5',
  `tags` varchar(200) DEFAULT NULL COMMENT '评价标签 JSON',
  `comment` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务评价表';

-- 菜单分组配置表
CREATE TABLE IF NOT EXISTS `menu_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `menu_index` varchar(100) NOT NULL COMMENT '菜单项路由路径',
  `group_name` varchar(50) NOT NULL COMMENT '所属分组名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_index` (`menu_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单分组配置表';

-- 仪表盘快捷入口配置表
CREATE TABLE IF NOT EXISTS `dashboard_quick_action` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `slot` int NOT NULL COMMENT '槽位 0-7',
  `label` varchar(50) NOT NULL COMMENT '显示名称',
  `path` varchar(100) NOT NULL COMMENT '路由路径',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slot` (`slot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仪表盘快捷入口配置表';

-- 仪表盘统计卡片配置表
CREATE TABLE IF NOT EXISTS `dashboard_stat_card` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `slot` int NOT NULL COMMENT '槽位',
  `stat_key` varchar(50) NOT NULL COMMENT '统计指标key',
  `label` varchar(50) NOT NULL COMMENT '显示名称',
  `path` varchar(100) NOT NULL COMMENT '路由路径',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slot` (`slot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仪表盘统计卡片配置表';
