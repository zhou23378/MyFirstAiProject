package com.salon.technician.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("technician_status")
public class TechnicianStatus extends BaseEntity {
    private Long employeeId;
    private String status;
    private String currentCustomerName;
    private String currentServiceName;
    private LocalDateTime lastStatusChanged;
}
