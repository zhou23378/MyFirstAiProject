package com.salon.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "预约请求DTO")
public class AppointmentReqDTO {

    @NotNull(message = "会员ID不能为空")
    @Schema(description = "会员ID", example = "1")
    private Long memberId;

    @NotNull(message = "服务项目ID不能为空")
    @Schema(description = "服务项目ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long serviceItemId;

    @Schema(description = "技师ID", example = "1")
    private Long employeeId;

    @NotNull(message = "预约日期不能为空")
    @Schema(description = "预约日期", example = "2026-05-20")
    private LocalDate appointmentDate;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "时长（分钟）", example = "60")
    private Integer duration;

    @Schema(description = "状态：1=已预约 2=已到店 3=已完成 4=已取消 5=爽约", example = "1")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
