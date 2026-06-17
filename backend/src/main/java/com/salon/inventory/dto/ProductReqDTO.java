package com.salon.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductReqDTO {
    @NotNull private Long categoryId;
    private Long supplierId;
    @NotBlank private String name;
    private String unit;
    private BigDecimal salePrice;
    private Integer stockQty;
    private Integer alertQty;
    private Integer status;
    private String remark;
}
