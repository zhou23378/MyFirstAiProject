package com.salon.consumption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求 DTO
 * <p>
 * 封装创建消费订单所需的完整信息
 * </p>
 */
@Data
@Schema(description = "创建订单请求")
public class CreateOrderDTO {

    /** 会员ID，必填 */
    @NotNull(message = "会员ID不能为空")
    @Schema(description = "会员ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    /** 支付方式：1=现金 2=余额 3=微信 4=支付宝 5=银行卡 6=储值卡 7=团购券 8=混合 */
    @Schema(description = "支付方式：1=现金 2=余额 3=微信 4=支付宝 5=银行卡 6=储值卡 7=团购券 8=混合", example = "1")
    private Integer payMethod;

    /** 组合支付明细（payMethod=8时使用） */
    @Schema(description = "组合支付明细")
    private List<PaymentDTO> payments;

    /** 支付金额（现金/微信部分） */
    @Schema(description = "支付金额", example = "100.00")
    private BigDecimal payAmount;

    /** 使用余额 */
    @Schema(description = "使用余额", example = "68.00")
    private BigDecimal balanceUsed;

    /** 优惠券码（可选） */
    @Schema(description = "优惠券码（可选）")
    private String couponCode;

    /** 支付备注 */
    @Schema(description = "支付备注")
    private String payRemark;

    /** 服务技师ID */
    @Schema(description = "服务技师ID")
    private Long employeeId;

    /** 订单明细列表，至少一项 */
    @NotEmpty(message = "订单明细不能为空")
    @Valid
    @Schema(description = "订单明细列表")
    private List<OrderItemDTO> items;

    /**
     * 订单明细 DTO
     */
    @Data
    @Schema(description = "订单明细")
    public static class OrderItemDTO {

        /** 服务项目ID，必填 */
        @NotNull(message = "服务项目ID不能为空")
        @Schema(description = "服务项目ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long itemId;

        /** 项目名称 */
        @Schema(description = "项目名称", example = "洗剪吹")
        private String itemName;

        /** 项目价格 */
        @Positive(message = "项目价格必须大于0")
        @Schema(description = "项目价格", example = "68.00")
        private BigDecimal itemPrice;

        /** 数量，默认 1 */
        @Positive(message = "数量必须大于0")
        @Schema(description = "数量", example = "1")
        private Integer quantity = 1;
    }

    /**
     * 组合支付明细 DTO
     */
    @Data
    @Schema(description = "单笔支付明细")
    public static class PaymentDTO {

        @Schema(description = "支付方式", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer payMethod;

        @Positive(message = "支付金额必须大于0")
        @Schema(description = "支付金额", example = "50.00", requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal amount;
    }
}
