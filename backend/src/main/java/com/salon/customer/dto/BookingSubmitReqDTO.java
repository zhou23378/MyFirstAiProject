package com.salon.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "顾客提交预约请求")
public class BookingSubmitReqDTO {
    @NotNull(message = "服务项目ID不能为空")
    @Schema(description = "服务项目ID")
    private Long serviceItemId;

    @Schema(description = "技师ID")
    private Long employeeId;

    @NotNull(message = "预约日期不能为空")
    @Schema(description = "预约日期", example = "2026-05-20")
    private LocalDate date;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", example = "09:00")
    private LocalTime startTime;
}
