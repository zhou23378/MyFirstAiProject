package com.salon.timer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TimerStartDTO {

    @NotNull
    private Long employeeId;

    @NotNull
    private Long memberId;

    /** 关联预约 ID（可为空） */
    private Long appointmentId;

    /** 关联排队 ID（可为空） */
    private Long queueId;

    /** 关联服务项目 ID（可为空） */
    private Long serviceItemId;

    /** 服务项目名称 */
    private String serviceItemName;

    /** 预计时长（分钟），默认 30 */
    private Integer plannedDuration = 30;
}
