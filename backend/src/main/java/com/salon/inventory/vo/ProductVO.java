package com.salon.inventory.vo;

import com.salon.inventory.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "商品视图")
public class ProductVO {

    public static ProductVO from(Product entity) {
        if (entity == null) return null;
        ProductVO vo = new ProductVO();
        vo.setId(entity.getId());
        vo.setCategoryId(entity.getCategoryId());
        vo.setSupplierId(entity.getSupplierId());
        vo.setName(entity.getName());
        vo.setPrice(entity.getSalePrice());
        vo.setStock(entity.getStockQty());
        vo.setUnit(entity.getUnit());
        vo.setAlertStock(entity.getAlertQty());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "库存数量")
    private Integer stock;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "预警库存")
    private Integer alertStock;

    @Schema(description = "状态 (1=启用 0=禁用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
