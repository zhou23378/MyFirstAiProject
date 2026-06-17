#!/usr/bin/env python3
"""Replace all bare int BusinessException(int, ...) calls with ErrorCode enum constants.
Covers 33+ occurrences across 7 files for Sprint 3b.
Strategy: Use exact string replacement on the BusinessException constructor call.
"""
import os
import re

BASE = "backend/src/main/java/com/salon"

# (file_path, [(old_str, new_str), ...])
files_and_replacements = [
    (
        f"{BASE}/consumption/service/impl/ConsumptionOrderServiceImpl.java",
        [
            ('BusinessException(400, "创建失败：资料缺失")', 'BusinessException(ErrorCode.BAD_REQUEST, "创建失败：资料缺失")'),
            ('BusinessException(400, "会员不存在")', 'BusinessException(ErrorCode.MEMBER_NOT_FOUND)'),
            ('BusinessException(400, "服务项目不存在")', 'BusinessException(ErrorCode.SERVICE_NOT_FOUND)'),
            ('BusinessException(400,\n                    String.format("支付金额合计(%.2f)与订单金额(%.2f)不一致", totalPayments, totalAmount))',
             'BusinessException(ErrorCode.BAD_REQUEST,\n                    String.format("支付金额合计(%.2f)与订单金额(%.2f)不一致", totalPayments, totalAmount))'),
            ('BusinessException(400, "优惠券不存在")', 'BusinessException(ErrorCode.COUPON_NOT_FOUND)'),
            ('BusinessException(400, "优惠券已使用")', 'BusinessException(ErrorCode.COUPON_USED)'),
            ('BusinessException(400, "优惠券已过期")', 'BusinessException(ErrorCode.COUPON_EXPIRED)'),
            ('BusinessException(400,\n                    String.format("未满足门槛，需满 ¥%.2f", conditionAmount))',
             'BusinessException(ErrorCode.COUPON_THRESHOLD_NOT_MET,\n                    String.format("未满足门槛，需满 ¥%.2f", conditionAmount))'),
            ('BusinessException(400, "优惠券已使用或已失效")', 'BusinessException(ErrorCode.COUPON_USED, "优惠券已使用或已失效")'),
            ('BusinessException(400, "余额不足")', 'BusinessException(ErrorCode.MEMBER_BALANCE_INSUFFICIENT)'),
            ('BusinessException(400, "更新会员失败")', 'BusinessException(ErrorCode.BAD_REQUEST, "更新会员失败")'),
            ('BusinessException(400, "该订单已退款或状态不符")', 'BusinessException(ErrorCode.ORDER_REFUNDED)'),
            ('BusinessException(500, "退款异常：会员不存在，余额退回失败")', 'BusinessException(ErrorCode.INTERNAL_ERROR, "退款异常：会员不存在，余额退回失败")'),
            ('BusinessException(500, "退款异常：会员不存在，积分退回失败")', 'BusinessException(ErrorCode.INTERNAL_ERROR, "退款异常：会员不存在，积分退回失败")'),
            ('BusinessException(500, "退款异常：优惠券恢复失败")', 'BusinessException(ErrorCode.INTERNAL_ERROR, "退款异常：优惠券恢复失败")'),
        ]
    ),
    (
        f"{BASE}/appointment/service/impl/AppointmentServiceImpl.java",
        [
            ('BusinessException(400, "开始时间必须早于结束时间")', 'BusinessException(ErrorCode.BAD_REQUEST, "开始时间必须早于结束时间")'),
            ('BusinessException(400,\n                String.format("该技师此时间段已忙碌（%s - %s 已有预约）"',
             'BusinessException(ErrorCode.BAD_REQUEST,\n                String.format("该技师此时间段已忙碌（%s - %s 已有预约）"'),
            ('BusinessException(400, "当前预约状态无法转为消费")', 'BusinessException(ErrorCode.BAD_REQUEST, "当前预约状态无法转为消费")'),
            ('BusinessException(400, "预约关联的服务项目不存在")', 'BusinessException(ErrorCode.SERVICE_NOT_FOUND)'),
            ('BusinessException(400, "预约状态已变更，无法转为消费")', 'BusinessException(ErrorCode.BAD_REQUEST, "预约状态已变更，无法转为消费")'),
            ('BusinessException(400, "当前预约状态不可到店")', 'BusinessException(ErrorCode.BAD_REQUEST, "当前预约状态不可到店")'),
            ('BusinessException(500, "到店更新会员数据失败：会员不存在")', 'BusinessException(ErrorCode.INTERNAL_ERROR, "到店更新会员数据失败：会员不存在")'),
            ('BusinessException(400, "预约不存在或状态变更无效")', 'BusinessException(ErrorCode.BAD_REQUEST, "预约不存在或状态变更无效")'),
        ]
    ),
    (
        f"{BASE}/coupon/service/impl/CouponServiceImpl.java",
        [
            ('BusinessException(400, "优惠券模板不存在")', 'BusinessException(ErrorCode.COUPON_NOT_FOUND)'),
            ('BusinessException(400, "该优惠券已停发")', 'BusinessException(ErrorCode.COUPON_DISABLED)'),
            ('BusinessException(400, "优惠券已发完")', 'BusinessException(ErrorCode.COUPON_EXHAUSTED)'),
            ('BusinessException(400, "优惠券已使用")', 'BusinessException(ErrorCode.COUPON_USED)'),
            ('BusinessException(400, "优惠券已过期")', 'BusinessException(ErrorCode.COUPON_EXPIRED)'),
            ('BusinessException(400,\n                    String.format("未满足门槛，需满 ¥%.2f", coupon.getConditionAmount()))',
             'BusinessException(ErrorCode.COUPON_THRESHOLD_NOT_MET,\n                    String.format("未满足门槛，需满 ¥%.2f", coupon.getConditionAmount()))'),
            ('BusinessException(400, "优惠券已使用或已失效")', 'BusinessException(ErrorCode.COUPON_USED, "优惠券已使用或已失效")'),
        ]
    ),
    (
        f"{BASE}/consumption/service/impl/ServiceCardServiceImpl.java",
        [
            ('BusinessException(400, "余额不足")', 'BusinessException(ErrorCode.MEMBER_BALANCE_INSUFFICIENT)'),
            ('BusinessException(400, "次卡已失效")', 'BusinessException(ErrorCode.BAD_REQUEST, "次卡已失效")'),
            ('BusinessException(400, "次卡次数已用完")', 'BusinessException(ErrorCode.BAD_REQUEST, "次卡次数已用完")'),
            ('BusinessException(400, "次卡已过期")', 'BusinessException(ErrorCode.BAD_REQUEST, "次卡已过期")'),
            ('BusinessException(400, "次卡已用完或已失效")', 'BusinessException(ErrorCode.BAD_REQUEST, "次卡已用完或已失效")'),
        ]
    ),
    (
        f"{BASE}/queue/service/impl/ServiceQueueServiceImpl.java",
        [
            ('BusinessException(500, "排队取号失败")', 'BusinessException(ErrorCode.INTERNAL_ERROR, "排队取号失败")'),
        ]
    ),
    (
        f"{BASE}/inventory/service/impl/StockRecordServiceImpl.java",
        [
            ('BusinessException(400, "商品不存在")', 'BusinessException(ErrorCode.PRODUCT_NOT_FOUND)'),
            ('BusinessException(400, "库存不足，当前库存：" + product.getStockQty())', 'BusinessException(ErrorCode.STOCK_INSUFFICIENT, "库存不足，当前库存：" + product.getStockQty())'),
        ]
    ),
    (
        f"{BASE}/technician/service/impl/TechnicianStatusServiceImpl.java",
        [
            ('BusinessException(500, "技师状态更新失败")', 'BusinessException(ErrorCode.INTERNAL_ERROR, "技师状态更新失败")'),
        ]
    ),
    (
        f"{BASE}/schedule/service/impl/AttendanceServiceImpl.java",
        [
            ('BusinessException(400, "今日已打卡上班")', 'BusinessException(ErrorCode.BAD_REQUEST, "今日已打卡上班")'),
            ('BusinessException(400, "请先打卡上班或今日已打卡下班")', 'BusinessException(ErrorCode.BAD_REQUEST, "请先打卡上班或今日已打卡下班")'),
        ]
    ),
]

def apply_replacements(filepath, replacements):
    print(f"\n=== Processing: {filepath} ===")
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except FileNotFoundError:
        print(f"  ✗ File not found, skipping")
        return 0
    
    count = 0
    for old_str, new_str in replacements:
        if old_str in content:
            content = content.replace(old_str, new_str)
            count += 1
            # Truncate for display
            short_old = old_str[:80] + ('...' if len(old_str) > 80 else '')
            short_new = new_str[:80] + ('...' if len(new_str) > 80 else '')
            print(f"  ✓ {short_old} → {short_new}")
        else:
            short_old = old_str[:80] + ('...' if len(old_str) > 80 else '')
            print(f"  ✗ NOT FOUND: {short_old}")
    
    if count > 0:
        with open(filepath, 'w', encoding='utf-8', newline='\n') as f:
            f.write(content)
        print(f"  → {count} replacements written")
    
    return count

total = 0
for filepath, replacements in files_and_replacements:
    total += apply_replacements(filepath, replacements)

print(f"\n{'='*50}")
print(f"Total replacements made: {total}")

# Verify no bare int BusinessException calls remain
print(f"\n--- Verification ---")
pattern = re.compile(r'new BusinessException\(\d+,')
remaining = 0
for filepath, _ in files_and_replacements:
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            for i, line in enumerate(f, 1):
                if pattern.search(line):
                    print(f"  REMAINING: {filepath}:{i}: {line.strip()}")
                    remaining += 1
    except FileNotFoundError:
        pass

if remaining == 0:
    print("  ✓ All bare int BusinessException calls replaced!")
else:
    print(f"  ✗ {remaining} bare int calls remaining")

# Also check ALL Java files for any remaining bare int calls
print(f"\n--- Full project scan ---")
all_remaining = 0
for root, dirs, files in os.walk(BASE):
    for f in files:
        if f.endswith('.java'):
            fpath = os.path.join(root, f)
            try:
                with open(fpath, 'r', encoding='utf-8') as fh:
                    for i, line in enumerate(fh, 1):
                        if pattern.search(line):
                            print(f"  {fpath}:{i}: {line.strip()}")
                            all_remaining += 1
            except Exception:
                pass

if all_remaining == 0:
    print("  ✓ No bare int BusinessException calls in any Java file!")
else:
    print(f"  {all_remaining} bare int call(s) still need attention")