package com.salon.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "顾客支付请求")
public class CustomerPayReqDTO {
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    @Schema(description = "支付金额", example = "68.00")
    private BigDecimal amount;

    @NotBlank(message = "支付方式不能为空")
    @Schema(description = "支付方式：BALANCE/WECHAT/ALIPAY", example = "BALANCE")
    private String payMethod;
}
