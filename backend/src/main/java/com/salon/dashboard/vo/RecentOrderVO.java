package com.salon.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "最近订单视图")
public class RecentOrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "服务项目列表")
    private String items;

    @Schema(description = "订单总额")
    private BigDecimal totalAmount;

    @Schema(description = "支付方式")
    private Integer payMethod;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "订单状态：1=正常 2=已退款")
    private Integer status;
}
