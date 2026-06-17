package com.salon.inventory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "库存记录分页视图")
public class StockRecordPageVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "产品ID")
    private Long productId;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "类型：1=入库 2=出库")
    private Integer type;

    @Schema(description = "数量")
    private Integer qty;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
