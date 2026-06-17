package com.salon.points.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_product")
public class PointsProduct extends BaseEntity {
    private String name;
    private String imageUrl;
    private Integer pointsPrice;
    private BigDecimal originalPrice;
    private Integer stockQty;
    private Integer exchangedCount;
    private String description;
    private Integer status;
    private Integer sortOrder;
}
