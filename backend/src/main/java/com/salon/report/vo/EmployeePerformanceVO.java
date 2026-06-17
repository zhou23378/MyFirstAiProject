package com.salon.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "员工绩效视图")
public class EmployeePerformanceVO {

    @Schema(description = "员工ID")
    private Long id;

    @Schema(description = "员工姓名")
    private String name;

    @Schema(description = "订单数")
    private Long orderCount;

    @Schema(description = "总营收")
    private BigDecimal totalRevenue;

    @Schema(description = "服务次数")
    private Long serviceCount;

    @Schema(description = "总提成")
    private BigDecimal totalCommission;
}
