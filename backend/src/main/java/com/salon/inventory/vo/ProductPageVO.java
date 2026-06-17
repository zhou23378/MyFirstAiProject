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
@Schema(description = "产品分页视图")
public class ProductPageVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "产品名称")
    private String name;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "销售价格")
    private BigDecimal salePrice;

    @Schema(description = "库存数量")
    private Integer stockQty;

    @Schema(description = "预警数量")
    private Integer alertQty;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
