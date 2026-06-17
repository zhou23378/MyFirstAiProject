package com.salon.points.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PointsProductReqDTO {
    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String imageUrl;

    @NotNull(message = "积分价格不能为空")
    @Min(value = 1, message = "积分价格至少为1")
    private Integer pointsPrice;

    private BigDecimal originalPrice;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能为负")
    private Integer stockQty;

    private String description;

    private Integer status;

    private Integer sortOrder;
}
