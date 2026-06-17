package com.salon.timer.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_timer")
public class ServiceTimer extends BaseEntity {
    private Long appointmentId;
    private Long queueId;
    private Long employeeId;
    private Long memberId;
    private Long serviceItemId;
    private String serviceItemName;
    private Integer plannedDuration;
    private Integer actualDuration;
    private Integer status;
    private LocalDateTime startedAt;
    private LocalDateTime pausedAt;
    private LocalDateTime completedAt;
    @TableField("alert_80_sent")
    private Integer alert80Sent;
    @TableField("alert_100_sent")
    private Integer alert100Sent;
}
