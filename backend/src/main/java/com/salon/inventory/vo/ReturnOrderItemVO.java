package com.salon.inventory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "退货订单明细")
public class ReturnOrderItemVO {

    @Schema(description = "ID")
    private Long id;

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
