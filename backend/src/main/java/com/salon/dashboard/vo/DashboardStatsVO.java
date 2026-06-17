package com.salon.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仪表盘统计视图")
public class DashboardStatsVO {

    @Schema(description = "会员总数")
    private Long memberCount;

    @Schema(description = "今日新增会员数")
    private Integer todayMember;

    @Schema(description = "今日订单数")
    private Integer todayOrder;

    @Schema(description = "今日营收")
    private BigDecimal todayRevenue;

    @Schema(description = "本月营收")
    private BigDecimal monthRevenue;

    @Schema(description = "员工总数")
    private Long employeeCount;

    @Schema(description = "服务项目总数")
    private Long serviceCount;

    @Schema(description = "营收趋势")
    private List<RevenueTrendItem> revenueTrend;

    @Schema(description = "支付方式分布")
    private List<PayMethodItem> payMethods;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "营收趋势项")
    public static class RevenueTrendItem {

        @Schema(description = "日期")
        private String date;

        @Schema(description = "营收金额")
        private BigDecimal value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "支付方式项")
    public static class PayMethodItem {

        @Schema(description = "支付方式名称")
        private String name;

        @Schema(description = "支付方式编码")
        private Integer payMethod;

        @Schema(description = "订单数")
        private Integer count;

        @Schema(description = "占比")
        private BigDecimal percentage;
    }
}
