package com.salon.schedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤分页视图")
public class AttendancePageVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "排班ID")
    private Long scheduleId;

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "上班打卡时间")
    private LocalDateTime clockIn;

    @Schema(description = "下班打卡时间")
    private LocalDateTime clockOut;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
