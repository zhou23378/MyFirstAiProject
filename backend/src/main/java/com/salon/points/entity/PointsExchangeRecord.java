package com.salon.points.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_exchange_record")
public class PointsExchangeRecord extends BaseEntity {
    private Long memberId;
    private String memberName;
    private String memberPhone;
    private Long productId;
    private String productName;
    private Integer pointsCost;
    private Integer quantity;
    private Integer status;
    private String remark;
    private Long operatorId;
    private LocalDateTime exchangedAt;
    private LocalDateTime claimedAt;
}
