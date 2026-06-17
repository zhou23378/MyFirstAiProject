package com.salon.queue.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_queue")
public class ServiceQueue extends BaseEntity {
    private Long memberId;
    private String memberName;
    private Long serviceItemId;
    private String serviceItemName;
    private Integer queueNumber;
    private Integer status;
    private Long assignedEmployeeId;
    private LocalDateTime waitSince;
}
