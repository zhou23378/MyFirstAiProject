package com.salon.dailyclose.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DailyCloseReqDTO {

    @NotNull(message = "日结日期不能为空")
    private LocalDate closeDate;

    private BigDecimal manualCash;

    private BigDecimal manualWechat;

    private BigDecimal manualAlipay;

    private String remark;
}
