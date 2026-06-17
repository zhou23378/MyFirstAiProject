package com.salon.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "退货订单明细")
public class ReturnOrderItemDTO {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "单位")
    private String unit;

    @NotNull(message = "数量不能为空")
    @Schema(description = "退货数量")
    private Integer qty;

    @Schema(description = "退货单价")
    private BigDecimal price;
}
