package com.salon.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "会员充值请求")
public class RechargeDTO {

    @NotNull(message = "充值金额不能为空")
    @Positive(message = "充值金额必须大于0")
    @Schema(description = "充值金额", example = "500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotNull(message = "支付方式不能为空")
    @Schema(description = "支付方式：1=现金 2=微信 3=支付宝 4=银行卡", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer payMethod;

    @Schema(description = "备注")
    private String remark;
}
