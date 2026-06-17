package com.salon.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "会员增长趋势")
public class MemberTrendVO {

    @Schema(description = "月份 (yyyy-MM)")
    private String period;

    @Schema(description = "新增会员数")
    private int newMembers;
}
