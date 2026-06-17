package com.salon.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_buy_order")
public class GroupBuyOrder extends BaseEntity {
    private Long templateId;
    private String orderNo;
    private Long leaderId;
    private String leaderName;
    private String leaderPhone;
    private BigDecimal groupPrice;
    private BigDecimal originalPrice;
    private Integer groupSize;
    private Integer currentSize;
    private LocalDateTime expireTime;
    private Integer status;
    private LocalDateTime completeTime;
}
