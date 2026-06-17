package com.salon.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "优惠券使用率统计")
public class CouponUsageVO {

    @Schema(description = "优惠券模板名称")
    private String templateName;

    @Schema(description = "已发放数量")
    private int issuedQty;

    @Schema(description = "已使用数量")
    private int usedQty;

    @Schema(description = "使用率(%)")
    private double usageRate;
}
