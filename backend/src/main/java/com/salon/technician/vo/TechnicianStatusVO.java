package com.salon.technician.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "技师状态视图")
public class TechnicianStatusVO {

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "状态（AVAILABLE/BUSY/BREAK/OFF_DUTY）")
    private String status;

    @Schema(description = "当前顾客姓名")
    private String currentCustomerName;

    @Schema(description = "当前服务名称")
    private String currentServiceName;

    @Schema(description = "最后状态变更时间")
    private LocalDateTime lastStatusChanged;
}
