package com.salon.schedule.vo;

import com.salon.schedule.entity.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "排班视图")
public class ScheduleVO {

    public static ScheduleVO from(Schedule entity) {
        if (entity == null) return null;
        ScheduleVO vo = new ScheduleVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "班次ID")
    private Long shiftId;

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
