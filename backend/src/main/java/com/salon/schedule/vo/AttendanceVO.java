package com.salon.schedule.vo;

import com.salon.schedule.entity.Attendance;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "考勤打卡视图")
public class AttendanceVO {

    public static AttendanceVO from(Attendance entity) {
        if (entity == null) return null;
        AttendanceVO vo = new AttendanceVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "排班ID")
    private Long scheduleId;

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "上班打卡")
    private LocalDateTime clockIn;

    @Schema(description = "下班打卡")
    private LocalDateTime clockOut;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
