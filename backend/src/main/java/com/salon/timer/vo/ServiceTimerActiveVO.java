package com.salon.timer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "服务计时器活跃视图")
public class ServiceTimerActiveVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "服务项目ID")
    private Long serviceItemId;

    @Schema(description = "服务项目名称")
    private String serviceItemName;

    @Schema(description = "计划时长（分钟）")
    private Integer plannedDuration;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "暂停时间")
    private LocalDateTime pausedAt;

    @Schema(description = "已过秒数")
    private Long elapsedSeconds;

    @Schema(description = "剩余秒数")
    private Long remainingSeconds;
}
