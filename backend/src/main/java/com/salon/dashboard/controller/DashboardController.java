package com.salon.dashboard.controller;

import com.salon.common.result.Result;
import com.salon.dashboard.vo.DashboardStatsVO;
import com.salon.dashboard.vo.RecentOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        DashboardStatsVO data = new DashboardStatsVO();

        Long memberCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM member", Long.class);
        data.setMemberCount(memberCount);

        Integer todayMember = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM member WHERE DATE(create_time) = CURDATE()", Integer.class);
        data.setTodayMember(todayMember);

        Integer todayOrder = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM consumption_order WHERE DATE(create_time) = CURDATE()", Integer.class);
        data.setTodayOrder(todayOrder);

        BigDecimal todayRevenue = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_amount), 0) FROM consumption_order WHERE DATE(create_time) = CURDATE()",
                BigDecimal.class);
        data.setTodayRevenue(todayRevenue);

        BigDecimal monthRevenue = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_amount), 0) FROM consumption_order WHERE MONTH(create_time) = MONTH(CURDATE()) AND YEAR(create_time) = YEAR(CURDATE())",
                BigDecimal.class);
        data.setMonthRevenue(monthRevenue);

        Long employeeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employee", Long.class);
        data.setEmployeeCount(employeeCount);

        Long serviceCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM service_item WHERE status = 1", Long.class);
        data.setServiceCount(serviceCount);

        // 近7日营收趋势
        List<Map<String, Object>> trendRows = jdbcTemplate.queryForList(
                "SELECT DATE(create_time) as date, COALESCE(SUM(total_amount), 0) as value " +
                "FROM consumption_order " +
                "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                "GROUP BY DATE(create_time) ORDER BY date");
        List<DashboardStatsVO.RevenueTrendItem> revenueTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            boolean found = false;
            for (Map<String, Object> row : trendRows) {
                if (date.equals(row.get("date").toString())) {
                    revenueTrend.add(new DashboardStatsVO.RevenueTrendItem(
                            date.substring(5), (BigDecimal) row.get("value")));
                    found = true;
                    break;
                }
            }
            if (!found) {
                revenueTrend.add(new DashboardStatsVO.RevenueTrendItem(
                        date.substring(5), BigDecimal.ZERO));
            }
        }
        data.setRevenueTrend(revenueTrend);

        // 支付方式分布
        List<Map<String, Object>> payRows = jdbcTemplate.queryForList(
                "SELECT pay_method, COUNT(*) as cnt FROM consumption_order GROUP BY pay_method");
        Map<Integer, String> payNameMap = Map.of(
                1, "现金支付", 2, "余额支付", 3, "微信支付",
                4, "支付宝", 5, "银行卡", 6, "储值卡", 7, "团购券", 8, "混合支付");
        long totalPay = payRows.stream().mapToLong(r -> ((Number) r.get("cnt")).longValue()).sum();
        List<DashboardStatsVO.PayMethodItem> payMethods = new ArrayList<>();
        for (Map<String, Object> row : payRows) {
            int method = ((Number) row.get("pay_method")).intValue();
            long cnt = ((Number) row.get("cnt")).longValue();
            DashboardStatsVO.PayMethodItem item = new DashboardStatsVO.PayMethodItem();
            item.setName(payNameMap.getOrDefault(method, "其他"));
            item.setPayMethod(method);
            item.setCount((int) cnt);
            item.setPercentage(totalPay > 0
                    ? BigDecimal.valueOf(cnt * 100.0 / totalPay).setScale(1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            payMethods.add(item);
        }
        data.setPayMethods(payMethods);

        return Result.success(data);
    }

    @GetMapping("/recent-orders")
    public Result<List<RecentOrderVO>> recentOrders() {
        String sql = "SELECT o.id, m.name AS memberName, o.total_amount AS totalAmount, " +
                     "o.pay_method AS payMethod, o.create_time AS createTime, " +
                     "e.name AS employeeName, o.status, " +
                     "(SELECT GROUP_CONCAT(item_name SEPARATOR '、') " +
                     " FROM consumption_order_item WHERE order_id = o.id) AS items " +
                     "FROM consumption_order o " +
                     "LEFT JOIN member m ON o.member_id = m.id " +
                     "LEFT JOIN employee e ON o.employee_id = e.id " +
                     "ORDER BY o.create_time DESC LIMIT 15";
        List<RecentOrderVO> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecentOrderVO.class));
        return Result.success(list);
    }
}
