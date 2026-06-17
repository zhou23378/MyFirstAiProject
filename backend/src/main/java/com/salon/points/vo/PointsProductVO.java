package com.salon.points.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PointsProductVO {
    private Long id;
    private String name;
    private String imageUrl;
    private Integer pointsPrice;
    private BigDecimal originalPrice;
    private Integer stockQty;
    private Integer exchangedCount;
    private String description;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
