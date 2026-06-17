package com.salon.groupbuy.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupBuyParticipantVO {
    private Long id;
    private Long orderId;
    private Long memberId;
    private String memberName;
    private String memberPhone;
    private BigDecimal joinPrice;
    private Integer status;
    private Integer isLeader;
    private LocalDateTime joinTime;
    private LocalDateTime completeTime;
    private LocalDateTime refundTime;
}
