package com.salon.technician.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TechnicianStatusReqDTO {
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @NotNull(message = "状态不能为空")
    private String status;

    private String currentCustomerName;
    private String currentServiceName;
}
