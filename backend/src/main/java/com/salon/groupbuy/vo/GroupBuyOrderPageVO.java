package com.salon.groupbuy.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupBuyOrderPageVO {
    private Long id;
    private Long templateId;
    private String orderNo;
    private String leaderName;
    private String leaderPhone;
    private BigDecimal groupPrice;
    private Integer groupSize;
    private Integer currentSize;
    private LocalDateTime expireTime;
    private Integer status;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
}
