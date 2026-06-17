package com.salon.commission.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CommissionSettlementItemVO {
    private Long orderId;
    private String orderNo;
    private String serviceNames;
    private BigDecimal orderAmount;
    private BigDecimal commissionAmount;
    private LocalDateTime orderTime;
}
