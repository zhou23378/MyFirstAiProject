package com.salon.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_buy_participant")
public class GroupBuyParticipant extends BaseEntity {
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
