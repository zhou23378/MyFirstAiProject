package com.salon.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("return_order_item")
@Schema(description = "退货订单明细实体")
public class ReturnOrderItem extends BaseEntity {

    @Schema(description = "退货订单ID")
    private Long orderId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "退货数量")
    private Integer qty;

    @Schema(description = "退货单价")
    private BigDecimal price;

    @Schema(description = "小计金额")
    private BigDecimal totalAmount;
}
