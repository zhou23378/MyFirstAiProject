package com.salon.groupbuy.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupBuyTemplatePageVO {
    private Long id;
    private String name;
    private String imageUrl;
    private BigDecimal groupPrice;
    private BigDecimal originalPrice;
    private Integer groupSize;
    private Integer expireHours;
    private Integer totalQty;
    private Integer issuedQty;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}
