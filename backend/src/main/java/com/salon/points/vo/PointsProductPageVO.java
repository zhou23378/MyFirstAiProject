package com.salon.points.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsProductPageVO {
    private Long id;
    private String name;
    private String imageUrl;
    private Integer pointsPrice;
    private Integer stockQty;
    private Integer exchangedCount;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
