package com.salon.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "日报/月报视图")
public class ReportPeriodVO {

    @Schema(description = "日期或月份字符串")
    private String period;

    @Schema(description = "订单数")
    private Long orderCount;

    @Schema(description = "营收")
    private BigDecimal revenue;

    @Schema(description = "积分")
    private Long points;
}
