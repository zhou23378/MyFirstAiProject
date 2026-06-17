package com.salon.dailyclose.vo;

import com.salon.dailyclose.entity.DailyClose;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "财务日结视图")
public class DailyCloseVO {

    public static DailyCloseVO from(DailyClose entity) {
        if (entity == null) return null;
        DailyCloseVO vo = new DailyCloseVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "日结日期")
    private LocalDate closeDate;

    @Schema(description = "系统现金")
    private BigDecimal systemCash;

    @Schema(description = "系统微信")
    private BigDecimal systemWechat;

    @Schema(description = "系统支付宝")
    private BigDecimal systemAlipay;

    @Schema(description = "系统余额")
    private BigDecimal systemBalance;

    @Schema(description = "系统次卡")
    private BigDecimal systemCard;

    @Schema(description = "系统银行卡")
    private BigDecimal systemBankCard;

    @Schema(description = "系统团购券")
    private BigDecimal systemGroupon;

    @Schema(description = "系统混合支付")
    private BigDecimal systemMixedPay;

    @Schema(description = "系统合计")
    private BigDecimal systemTotal;

    @Schema(description = "人工现金")
    private BigDecimal manualCash;

    @Schema(description = "人工微信")
    private BigDecimal manualWechat;

    @Schema(description = "人工支付宝")
    private BigDecimal manualAlipay;

    @Schema(description = "人工合计")
    private BigDecimal manualTotal;

    @Schema(description = "差异金额")
    private BigDecimal diffAmount;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "锁定人")
    private String lockedBy;

    @Schema(description = "锁定时间")
    private LocalDateTime lockedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
