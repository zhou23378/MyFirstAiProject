package com.salon.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "退货订单请求")
public class ReturnOrderReqDTO {

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "关联采购订单ID")
    private Long originalOrderId;

    @NotEmpty(message = "退货明细不能为空")
    @Valid
    @Schema(description = "退货明细")
    private List<ReturnOrderItemDTO> items;

    @Schema(description = "退货原因")
    private String reason;

    @Schema(description = "备注")
    private String remark;
}
