package com.salon.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order_item")
@Schema(description = "采购订单明细实体")
public class PurchaseOrderItem extends BaseEntity {

    @Schema(description = "采购订单ID")
    private Long orderId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "采购数量")
    private Integer qty;

    @Schema(description = "采购单价")
    private BigDecimal price;

    @Schema(description = "小计金额")
    private BigDecimal totalAmount;

    @Schema(description = "已收数量")
    private Integer receivedQty;
}
