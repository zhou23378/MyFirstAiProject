package com.salon.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "服务排行视图")
public class ServiceRankVO {

    @Schema(description = "服务项目名称")
    private String itemName;

    @Schema(description = "服务次数")
    private Long count;

    @Schema(description = "营收")
    private BigDecimal revenue;
}
