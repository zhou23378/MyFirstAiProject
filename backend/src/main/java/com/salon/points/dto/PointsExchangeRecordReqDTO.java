package com.salon.points.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointsExchangeRecordReqDTO {
    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Min(value = 1, message = "兑换数量至少为1")
    private Integer quantity;
}
