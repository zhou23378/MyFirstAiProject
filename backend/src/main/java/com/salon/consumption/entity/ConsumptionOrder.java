package com.salon.consumption.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 消费订单实体
 * <p>
 * 存储会员的消费订单主表信息
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consumption_order")
@Schema(description = "消费订单实体")
public class ConsumptionOrder extends BaseEntity {

    /** 会员ID */
    @Schema(description = "会员ID", example = "1")
    private Long memberId;

    /** 订单总金额 */
    @Schema(description = "订单总金额", example = "168.00")
    private BigDecimal totalAmount;

    /** 支付方式：1=现金 2=余额 3=微信 4=混合 */
    @Schema(description = "支付方式：1=现金 2=余额 3=微信 4=混合", example = "1")
    private Integer payMethod;

    /** 支付金额（现金/微信部分） */
    @Schema(description = "支付金额", example = "100.00")
    private BigDecimal payAmount;

    /** 使用余额 */
    @Schema(description = "使用余额", example = "68.00")
    private BigDecimal balanceUsed;

    /** 获得积分 */
    @Schema(description = "获得积分", example = "168")
    private Integer pointsEarned;

    /** 订单状态：1=正常 2=已退款 */
    @Schema(description = "订单状态：1=正常 2=已退款", example = "1")
    private Integer status;

    /** 支付备注 */
    @Schema(description = "支付备注")
    private String payRemark;

    /** 使用优惠券ID */
    @Schema(description = "使用优惠券ID")
    private Long couponId;

    /** 优惠券优惠金额 */
    @Schema(description = "优惠券优惠金额", example = "10.00")
    private BigDecimal couponDiscount;

    /** 服务技师ID */
    @Schema(description = "服务技师ID")
    private Long employeeId;

    /** 提成金额 */
    @Schema(description = "提成金额", example = "16.80")
    private BigDecimal commissionAmount;
}
