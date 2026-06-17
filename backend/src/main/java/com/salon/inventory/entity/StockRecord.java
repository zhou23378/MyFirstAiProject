package com.salon.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_record")
public class StockRecord extends BaseEntity {
    private Long productId;
    private Integer type;
    private Integer qty;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private Long supplierId;
    private String operator;
    private String remark;
}
