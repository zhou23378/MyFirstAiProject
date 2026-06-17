package com.salon.inventory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "采购订单分页")
public class PurchaseOrderPageVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "采购总金额")
    private BigDecimal totalAmount;

    @Schema(description = "商品种类数")
    private Integer itemCount;

    @Schema(description = "商品总数量")
    private Integer totalQty;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
