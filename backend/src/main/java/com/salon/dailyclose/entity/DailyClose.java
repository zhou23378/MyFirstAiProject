package com.salon.dailyclose.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.salon.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("daily_close")
public class DailyClose extends BaseEntity {
    private LocalDate closeDate;
    private BigDecimal systemCash;
    private BigDecimal systemWechat;
    private BigDecimal systemAlipay;
    private BigDecimal systemBalance;
    private BigDecimal systemCard;
    private BigDecimal systemBankCard;
    private BigDecimal systemGroupon;
    private BigDecimal systemMixedPay;
    private BigDecimal systemTotal;
    private BigDecimal manualCash;
    private BigDecimal manualWechat;
    private BigDecimal manualAlipay;
    private BigDecimal manualTotal;
    private BigDecimal diffAmount;
    private Integer status;
    private String remark;
    private String lockedBy;
    private LocalDateTime lockedAt;
}
