package com.salon.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {
    private Long categoryId;
    private Long supplierId;
    private String name;
    private String unit;
    private BigDecimal salePrice;
    private Integer stockQty;
    private Integer alertQty;
    private Integer status;
    private String remark;
}
