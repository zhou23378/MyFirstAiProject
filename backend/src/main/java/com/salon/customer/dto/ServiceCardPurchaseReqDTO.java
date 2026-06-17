package com.salon.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "顾客购买次卡请求")
public class ServiceCardPurchaseReqDTO {

    @NotNull(message = "服务项目不能为空")
    @Schema(description = "服务项目ID")
    private Long serviceItemId;

    @NotNull(message = "次数不能为空")
    @Min(value = 1, message = "次数至少为1")
    @Schema(description = "购买次数")
    private Integer totalCount;
}
