package com.salon.appointment.vo;

import com.salon.appointment.entity.Appointment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Schema(description = "预约视图")
public class AppointmentVO {

    public static AppointmentVO from(Appointment entity) {
        if (entity == null) return null;
        AppointmentVO vo = new AppointmentVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "服务项目ID")
    private Long serviceItemId;

    @Schema(description = "技师ID")
    private Long employeeId;

    @Schema(description = "预约日期")
    private LocalDate appointmentDate;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "时长（分钟）")
    private Integer duration;

    @Schema(description = "状态：1=已预约 2=已到店 3=已完成 4=已取消 5=爽约")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
