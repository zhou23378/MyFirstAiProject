package com.salon.consumption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "挂单请求")
public class SuspendOrderReqDTO {

    @NotNull(message = "会员ID不能为空")
    @Schema(description = "会员ID")
    private Long memberId;

    @NotEmpty(message = "订单明细不能为空")
    @Valid
    @Schema(description = "订单明细列表")
    private List<OrderItemDTO> items;

    @Schema(description = "服务技师ID")
    private Long employeeId;

    @Data
    @Schema(description = "订单明细")
    public static class OrderItemDTO {

        @NotNull(message = "服务项目ID不能为空")
        @Schema(description = "服务项目ID")
        private Long itemId;

        @Schema(description = "项目名称")
        private String itemName;

        @Positive(message = "项目价格必须大于0")
        @Schema(description = "项目价格")
        private BigDecimal itemPrice;

        @Positive(message = "数量必须大于0")
        @Schema(description = "数量")
        private Integer quantity = 1;
    }
}
