package com.salon.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会员消费排行视图")
public class MemberSpendingVO {

    @Schema(description = "会员ID")
    private Long id;

    @Schema(description = "会员姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "订单数")
    private Long orderCount;

    @Schema(description = "累计消费金额")
    private BigDecimal totalSpent;
}
