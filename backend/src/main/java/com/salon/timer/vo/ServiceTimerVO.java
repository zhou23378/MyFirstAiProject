package com.salon.timer.vo;

import com.salon.timer.entity.ServiceTimer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@Schema(description = "服务计时视图")
public class ServiceTimerVO {

    public static ServiceTimerVO from(ServiceTimer entity) {
        if (entity == null) return null;
        ServiceTimerVO vo = new ServiceTimerVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "排队ID")
    private Long queueId;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "服务项目ID")
    private Long serviceItemId;

    @Schema(description = "服务项目名称")
    private String serviceItemName;

    @Schema(description = "计划时长(分钟)")
    private Integer plannedDuration;

    @Schema(description = "实际时长(分钟)")
    private Integer actualDuration;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "暂停时间")
    private LocalDateTime pausedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "80%提醒已发")
    private Integer alert80Sent;

    @Schema(description = "100%提醒已发")
    private Integer alert100Sent;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
