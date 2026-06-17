package com.salon.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 预约转消费请求 DTO
 */
@Data
@Schema(description = "预约转消费请求")
public class ConvertToOrderDTO {

    @NotNull(message = "支付方式不能为空")
    @Schema(description = "支付方式：1=现金 2=余额 3=微信 4=支付宝 5=银行卡 6=储值卡 7=团购券 8=混合", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer payMethod;

    @Schema(description = "支付备注")
    private String payRemark;
}
