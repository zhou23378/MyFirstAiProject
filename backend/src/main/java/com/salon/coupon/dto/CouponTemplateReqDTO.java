package com.salon.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponTemplateReqDTO {
    @NotBlank private String name;
    @NotNull private Integer type;
    private BigDecimal conditionAmount;
    @NotNull private BigDecimal discountValue;
    @NotNull private Integer validDays;
    private Integer totalQty;
    private Integer status;
    private String remark;
}
