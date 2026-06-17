package com.salon.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Schema(description = "支付结果")
public class PaymentResultDTO {
    @Schema(description = "支付ID")
    private Long paymentId;

    @Schema(description = "支付状态: PENDING|SUCCESS|FAILED")
    private String status;

    @Schema(description = "二维码链接（微信/支付宝扫码支付）")
    private String qrCodeUrl;

    @Schema(description = "支付金额")
    private BigDecimal amount;
}
