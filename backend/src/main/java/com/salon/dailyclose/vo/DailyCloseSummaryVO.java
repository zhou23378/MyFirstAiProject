package com.salon.dailyclose.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "日结汇总视图（系统数据与手动数据合并）")
public class DailyCloseSummaryVO {

    @Schema(description = "关账日期")
    private String closeDate;

    @Schema(description = "系统统计：现金")
    private BigDecimal systemCash;

    @Schema(description = "系统统计：余额")
    private BigDecimal systemBalance;

    @Schema(description = "系统统计：微信")
    private BigDecimal systemWechat;

    @Schema(description = "系统统计：支付宝")
    private BigDecimal systemAlipay;

    @Schema(description = "系统统计：银行卡")
    private BigDecimal systemBankCard;

    @Schema(description = "系统统计：次卡")
    private BigDecimal systemCard;

    @Schema(description = "系统统计：团购")
    private BigDecimal systemGroupon;

    @Schema(description = "系统统计：混合支付")
    private BigDecimal systemMixedPay;

    @Schema(description = "系统统计：合计")
    private BigDecimal systemTotal;

    @Schema(description = "手动日结记录ID（可为空）")
    private Long id;

    @Schema(description = "手动日结：现金")
    private BigDecimal manualCash;

    @Schema(description = "手动日结：微信")
    private BigDecimal manualWechat;

    @Schema(description = "手动日结：支付宝")
    private BigDecimal manualAlipay;

    @Schema(description = "手动日结：合计")
    private BigDecimal manualTotal;

    @Schema(description = "差额（手动合计 - 系统合计）")
    private BigDecimal diffAmount;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "锁定人")
    private String lockedBy;
}
