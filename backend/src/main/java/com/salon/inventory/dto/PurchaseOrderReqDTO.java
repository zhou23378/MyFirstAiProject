package com.salon.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "采购订单请求")
public class PurchaseOrderReqDTO {

    @Schema(description = "供应商ID")
    private Long supplierId;

    @NotEmpty(message = "采购明细不能为空")
    @Valid
    @Schema(description = "采购明细")
    private List<PurchaseOrderItemDTO> items;

    @Schema(description = "备注")
    private String remark;
}
