package com.salon.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付状态视图")
public class PaymentStatusVO {

    @Schema(description = "支付状态")
    private String status;
}
