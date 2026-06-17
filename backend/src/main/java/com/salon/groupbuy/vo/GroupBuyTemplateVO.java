package com.salon.groupbuy.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GroupBuyTemplateVO {
    private Long id;
    private String name;
    private String imageUrl;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal groupPrice;
    private Integer groupSize;
    private Integer expireHours;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalQty;
    private Integer issuedQty;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
