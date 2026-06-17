package com.salon.schedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "周排班视图")
public class ScheduleWeekVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "班次ID")
    private Long shiftId;

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "班次名称")
    private String shiftName;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "颜色")
    private String color;
}
