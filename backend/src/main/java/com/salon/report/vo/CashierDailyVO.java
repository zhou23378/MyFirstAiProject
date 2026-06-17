package com.salon.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收银日报视图")
public class CashierDailyVO {

    @Schema(description = "支付方式")
    private Integer payMethod;

    @Schema(description = "订单数")
    private Long orderCount;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "余额抵扣")
    private BigDecimal balanceUsed;
}
