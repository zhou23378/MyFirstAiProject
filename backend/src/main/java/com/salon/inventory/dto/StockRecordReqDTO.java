package com.salon.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockRecordReqDTO {
    @NotNull private Long productId;
    @NotNull private Integer type;
    @NotNull private Integer qty;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private Long supplierId;
    private String remark;
}
