package com.salon.commission.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("commission_settlement")
public class CommissionSettlement extends BaseEntity {
    private Long employeeId;
    private String employeeName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer orderCount;
    private BigDecimal totalCommission;
    private Integer status;
    private LocalDateTime confirmedAt;
    private LocalDateTime paidAt;
    private String remark;
}
