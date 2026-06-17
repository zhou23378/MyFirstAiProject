package com.salon.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order")
@Schema(description = "采购订单实体")
public class PurchaseOrder extends BaseEntity {

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "供应商ID")
    private Long supplierId;

    @Schema(description = "状态：0=草稿 1=已提交 2=已审批 3=已收货 4=已取消")
    private Integer status;

    @Schema(description = "采购总金额")
    private BigDecimal totalAmount;

    @Schema(description = "商品种类数")
    private Integer itemCount;

    @Schema(description = "商品总数量")
    private Integer totalQty;

    @Schema(description = "申请人")
    private String applicant;

    @Schema(description = "审批人")
    private String approver;

    @Schema(description = "审批时间")
    private LocalDateTime approvedTime;

    @Schema(description = "收货时间")
    private LocalDateTime receivedTime;

    @Schema(description = "备注")
    private String remark;
}
