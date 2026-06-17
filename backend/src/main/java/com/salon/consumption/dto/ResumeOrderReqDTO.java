package com.salon.consumption.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "取单结算请求")
public class ResumeOrderReqDTO {

    @Schema(description = "支付方式：1=现金 2=余额 3=微信 4=支付宝 5=银行卡 6=储值卡 7=团购券 8=混合")
    private Integer payMethod;

    @Schema(description = "组合支付明细（payMethod=8时使用）")
    private List<CreateOrderDTO.PaymentDTO> payments;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "使用余额")
    private BigDecimal balanceUsed;

    @Schema(description = "优惠券码")
    private String couponCode;

    @Schema(description = "支付备注")
    private String payRemark;

    @Schema(description = "服务技师ID")
    private Long employeeId;
}
