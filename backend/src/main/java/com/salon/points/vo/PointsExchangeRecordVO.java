package com.salon.points.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsExchangeRecordVO {
    private Long id;
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
    private LocalDateTime createTime;
}
