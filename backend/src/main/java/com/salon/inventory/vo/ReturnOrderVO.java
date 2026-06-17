package com.salon.inventory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "退货订单详情")
public class ReturnOrderVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "退货单号")
    private String orderNo;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "关联采购订单ID")
    private Long originalOrderId;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "退货总金额")
    private BigDecimal totalAmount;

    @Schema(description = "商品种类数")
    private Integer itemCount;

    @Schema(description = "退货总数量")
    private Integer totalQty;

    @Schema(description = "退货原因")
    private String reason;

    @Schema(description = "申请人")
    private String applicant;

    @Schema(description = "审批人")
    private String approver;

    @Schema(description = "审批时间")
    private LocalDateTime approvedTime;

    @Schema(description = "完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "退货明细")
    private List<ReturnOrderItemVO> items;
}
