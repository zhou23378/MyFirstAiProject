package com.salon.technician.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "技师今日业绩统计")
public class TechnicianPerformanceVO {

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "今日服务单数")
    private Long todayOrders;

    @Schema(description = "今日营收")
    private BigDecimal todayRevenue;
}
