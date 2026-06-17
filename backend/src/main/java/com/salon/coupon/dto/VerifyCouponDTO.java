package com.salon.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VerifyCouponDTO {
    @NotBlank private String code;
    @NotNull private Long orderId;
    @NotNull private BigDecimal orderAmount;
}
