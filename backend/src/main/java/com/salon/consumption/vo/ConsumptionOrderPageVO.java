package com.salon.consumption.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消费订单分页视图")
public class ConsumptionOrderPageVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "技师ID")
    private Long employeeId;

    @Schema(description = "技师姓名")
    private String employeeName;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "使用余额")
    private BigDecimal balanceUsed;

    @Schema(description = "获得积分")
    private Integer pointsEarned;

    @Schema(description = "订单状态：1=正常 2=已退款")
    private Integer status;

    @Schema(description = "支付备注")
    private String payRemark;

    @Schema(description = "优惠券优惠金额")
    private BigDecimal couponDiscount;

    @Schema(description = "提成金额")
    private BigDecimal commissionAmount;

    @Schema(description = "支付方式：1=现金 2=余额 3=微信 4=支付宝 5=银行卡 6=储值卡 7=团购券 8=混合")
    private Integer payMethod;

    @Schema(description = "服务项目列表")
    private String items;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
