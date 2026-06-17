package com.salon.appointment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 预约实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("appointment")
@Schema(description = "预约实体")
public class Appointment extends BaseEntity {

    @Schema(description = "会员ID", example = "1")
    private Long memberId;

    @Schema(description = "服务项目ID", example = "1")
    private Long serviceItemId;

    @Schema(description = "技师ID", example = "1")
    private Long employeeId;

    @Schema(description = "预约日期", example = "2026-05-20")
    private LocalDate appointmentDate;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "时长（分钟）", example = "30")
    private Integer duration;

    @Schema(description = "状态：1=已预约 2=已到店 3=已完成 4=已取消 5=爽约", example = "1")
    private Integer status;

    @Schema(description = "是否已发送提醒：0=未发送 1=已发送")
    private Integer reminded;

    @Schema(description = "备注")
    private String remark;
}
