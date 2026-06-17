package com.salon.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_buy_template")
public class GroupBuyTemplate extends BaseEntity {
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
}
