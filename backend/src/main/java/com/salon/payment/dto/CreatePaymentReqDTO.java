package com.salon.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "创建支付请求")
public class CreatePaymentReqDTO {
    @NotNull(message = "订单ID不能为空")
    @Schema(description = "消费订单ID")
    private Long orderId;

    @NotBlank(message = "支付方式不能为空")
    @Schema(description = "支付方式: WECHAT|ALIPAY|BALANCE")
    private String method;

    @NotNull(message = "支付金额不能为空")
    @Schema(description = "支付金额")
    private BigDecimal amount;
}
