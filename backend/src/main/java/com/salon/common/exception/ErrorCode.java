package com.salon.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 * <p>
 * 定义系统中所有业务错误码，统一管理。新增错误码必须在此枚举中定义，
 * 禁止在 Service 层使用 {@code new BusinessException(400, "msg")} 裸写。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ──────────── 通用 ────────────
    /** 参数错误 */
    BAD_REQUEST(400, "请求参数错误"),
    /** 未授权 */
    UNAUTHORIZED(401, "未登录或Token已过期"),
    /** 无权限 */
    FORBIDDEN(403, "无权限访问"),
    /** 资源不存在 */
    NOT_FOUND(404, "请求的资源不存在"),
    /** 服务器内部错误 */
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ──────────── 认证授权 ────────────
    /** 用户名或密码错误 */
    LOGIN_FAILED(401, "用户名或密码错误"),
    /** 缺少认证信息 */
    AUTH_MISSING(401, "缺少认证信息"),
    /** 无权执行该操作 */
    AUTH_FORBIDDEN(403, "无权执行该操作"),
    /** 手机号已存在 */
    PHONE_EXISTS(400, "手机号已存在"),

    // ──────────── 短信 ────────────
    /** 短信发送失败 */
    SMS_SEND_FAILED(500, "短信发送失败"),
    /** 验证码无效 */
    SMS_CODE_INVALID(400, "验证码无效或已过期"),

    // ──────────── 会员 ────────────
    /** 会员不存在 */
    MEMBER_NOT_FOUND(404, "会员不存在"),
    /** 余额不足 */
    MEMBER_BALANCE_INSUFFICIENT(400, "会员余额不足"),

    // ──────────── 服务项目 ────────────
    /** 服务项目不存在 */
    SERVICE_NOT_FOUND(404, "服务项目不存在"),

    // ──────────── 订单 ────────────
    /** 订单不存在 */
    ORDER_NOT_FOUND(404, "订单不存在"),
    /** 订单已退款 */
    ORDER_REFUNDED(400, "订单已退款"),
    /** 订单已支付，无法重复结算 */
    ORDER_ALREADY_PAID(400, "订单已支付，无法重复结算"),

    // ──────────── 支付 ────────────
    /** 支付明细写入失败 */
    PAYMENT_DETAIL_WRITE_FAILED(500, "支付明细写入失败"),

    // ──────────── 优惠券 ────────────
    /** 优惠券不存在 */
    COUPON_NOT_FOUND(404, "优惠券不存在"),
    /** 优惠券已使用 */
    COUPON_USED(400, "优惠券已使用"),
    /** 优惠券已过期 */
    COUPON_EXPIRED(400, "优惠券已过期"),
    /** 优惠券已停发 */
    COUPON_DISABLED(400, "优惠券已停发"),
    /** 优惠券已发完 */
    COUPON_EXHAUSTED(400, "优惠券已发完"),
    /** 未满足使用门槛 */
    COUPON_THRESHOLD_NOT_MET(400, "未满足优惠券使用门槛"),

    // ──────────── 次卡 ────────────
    /** 次卡不存在 */
    CARD_NOT_FOUND(404, "次卡不存在"),
    /** 次卡已失效 */
    CARD_DISABLED(400, "次卡已失效"),
    /** 次卡次数已用完 */
    CARD_EXHAUSTED(400, "次卡次数已用完"),
    /** 次卡已过期 */
    CARD_EXPIRED(400, "次卡已过期"),

    // ──────────── 预约 ────────────
    /** 预约不存在 */
    APPOINTMENT_NOT_FOUND(404, "预约不存在"),
    /** 预约状态不符 */
    APPOINTMENT_STATUS_INVALID(400, "预约状态不符，无法执行此操作"),

    // ──────────── 服务计时 ────────────
    /** 计时记录不存在 */
    TIMER_NOT_FOUND(404, "计时记录不存在"),
    /** 计时状态不符 */
    TIMER_STATUS_INVALID(400, "计时状态不符，无法执行此操作"),

    // ──────────── 排队 ────────────
    /** 排队记录已处理 */
    QUEUE_ALREADY_PROCESSED(400, "排队记录已处理"),
    /** 技师不可用 */
    QUEUE_TECHNICIAN_UNAVAILABLE(400, "技师当前不可用"),

    // ──────────── 考勤 ────────────
    /** 今日已打卡 */
    ATTENDANCE_ALREADY_CLOCKED(400, "今日已打卡"),
    /** 请先打卡上班 */
    ATTENDANCE_NOT_CLOCKED_IN(400, "请先打卡上班"),

    // ──────────── 库存 ────────────
    /** 商品不存在 */
    PRODUCT_NOT_FOUND(404, "商品不存在"),
    /** 商品分类不存在 */
    PRODUCT_CATEGORY_NOT_FOUND(404, "商品分类不存在"),
    /** 库存不足 */
    STOCK_INSUFFICIENT(400, "库存不足"),

    // ──────────── AI ────────────
    /** AI服务不可用 */
    AI_SERVICE_UNAVAILABLE(503, "AI服务暂时不可用"),
    /** AI搜索失败 */
    AI_SEARCH_FAILED(500, "AI搜索失败"),

    // ──────────── 采购订单 ────────────
    /** 采购订单不存在 */
    PURCHASE_ORDER_NOT_FOUND(404, "采购订单不存在"),
    /** 采购订单状态不符 */
    PURCHASE_ORDER_STATUS_INVALID(400, "采购订单状态不符，无法执行此操作"),

    // ──────────── 退货订单 ────────────
    /** 退货订单不存在 */
    RETURN_ORDER_NOT_FOUND(404, "退货订单不存在"),
    /** 退货订单状态不符 */
    RETURN_ORDER_STATUS_INVALID(400, "退货订单状态不符，无法执行此操作"),

    // ──────────── 提成结算 ────────────
    /** 提成结算记录不存在 */
    COMMISSION_SETTLEMENT_NOT_FOUND(404, "提成结算记录不存在"),
    /** 该时间段已有结算记录 */
    COMMISSION_SETTLEMENT_PERIOD_EXISTS(400, "该时间段已有结算记录"),
    /** 结算状态不符 */
    COMMISSION_SETTLEMENT_STATUS_INVALID(400, "结算状态不符，无法执行此操作"),
    /** 该时间段内无可结算数据 */
    COMMISSION_SETTLEMENT_NO_DATA(400, "该时间段内无可结算的订单数据"),

    // ──────────── 积分商城 ────────────
    POINTS_PRODUCT_NOT_FOUND(404, "积分商品不存在"),
    POINTS_PRODUCT_STOCK_INSUFFICIENT(400, "商品库存不足"),
    POINTS_PRODUCT_OFF_SHELF(400, "商品已下架"),
    MEMBER_POINTS_INSUFFICIENT(400, "会员积分不足"),
    POINTS_EXCHANGE_RECORD_NOT_FOUND(404, "兑换记录不存在"),
    POINTS_EXCHANGE_STATUS_INVALID(400, "兑换状态不符，无法执行此操作"),

    // ──────────── 拼团 ────────────
    GROUP_BUY_TEMPLATE_NOT_FOUND(404, "拼团模板不存在"),
    GROUP_BUY_TEMPLATE_NOT_ACTIVE(400, "拼团活动未启用"),
    GROUP_BUY_TEMPLATE_NOT_IN_TIME(400, "拼团活动不在有效时间内"),
    GROUP_BUY_TEMPLATE_EXHAUSTED(400, "拼团活动已满额"),
    GROUP_BUY_TEMPLATE_HAS_ORDERS(400, "该模板已有成团记录，无法编辑"),
    GROUP_BUY_ORDER_NOT_FOUND(404, "拼团团单不存在"),
    GROUP_BUY_ORDER_FULL(400, "该团已满员"),
    GROUP_BUY_ORDER_EXPIRED(400, "该团已过期"),
    GROUP_BUY_ORDER_CLOSED(400, "该团已结束"),
    GROUP_BUY_ALREADY_JOINED(400, "您已参与该团"),
    GROUP_BUY_MEMBER_BALANCE_INSUFFICIENT(400, "余额不足，无法参与拼团"),
    GROUP_BUY_PARTICIPANT_NOT_FOUND(404, "参团记录不存在"),
    GROUP_BUY_PARTICIPANT_STATUS_INVALID(400, "参团状态不符，无法执行此操作"),

    // ──────────── 文件 ────────────
    /** 文件大小超限 */
    FILE_SIZE_EXCEEDED(400, "文件大小超过限制"),
    /** 文件格式不支持 */
    FILE_FORMAT_UNSUPPORTED(400, "文件格式不支持");

    /** HTTP 状态码 */
    private final int code;

    /** 错误消息 */
    private final String message;
}
