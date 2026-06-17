package com.salon.commission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CommissionSettlementReqDTO {
    @NotNull(message = "起始日期不能为空")
    private LocalDate periodStart;

    @NotNull(message = "截止日期不能为空")
    private LocalDate periodEnd;
}
