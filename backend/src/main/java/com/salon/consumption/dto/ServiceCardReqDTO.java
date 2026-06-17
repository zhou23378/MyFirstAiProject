package com.salon.consumption.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceCardReqDTO {
    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @NotNull(message = "服务项目ID不能为空")
    private Long serviceItemId;

    @NotNull(message = "总次数不能为空")
    @Min(value = 1, message = "总次数至少为1")
    private Integer totalCount;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;
}
