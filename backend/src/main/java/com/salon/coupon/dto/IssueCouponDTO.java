package com.salon.coupon.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IssueCouponDTO {
    @NotNull private Long templateId;
    @NotNull private Long memberId;
}
