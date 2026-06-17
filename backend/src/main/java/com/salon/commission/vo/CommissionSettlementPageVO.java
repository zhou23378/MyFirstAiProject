package com.salon.commission.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CommissionSettlementPageVO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer orderCount;
    private BigDecimal totalCommission;
    private Integer status;
    private LocalDateTime createTime;
}
