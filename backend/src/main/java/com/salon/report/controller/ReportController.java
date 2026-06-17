package com.salon.report.controller;

import com.salon.common.result.Result;
import com.salon.report.vo.CashierDailyVO;
import com.salon.report.vo.CouponUsageVO;
import com.salon.report.vo.EmployeePerformanceVO;
import com.salon.report.vo.MemberSpendingVO;
import com.salon.report.vo.MemberTrendVO;
import com.salon.report.vo.ReportPeriodVO;
import com.salon.report.vo.ServiceRankVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/daily")
    public Result<List<ReportPeriodVO>> daily(@RequestParam(required = false) String start,
                                               @RequestParam(required = false) String end) {
        String sql = "SELECT DATE(create_time) as period, " +
                     "COUNT(*) as orderCount, " +
                     "COALESCE(SUM(total_amount), 0) as revenue, " +
                     "COALESCE(SUM(points_earned), 0) as points " +
                     "FROM consumption_order " +
                     "WHERE (? IS NULL OR create_time >= ?) " +
                     "AND (? IS NULL OR create_time <= ?) " +
                     "GROUP BY DATE(create_time) " +
                     "ORDER BY period DESC";
        return Result.success(jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(ReportPeriodVO.class), start, start, end, end));
    }

    @GetMapping("/monthly")
    public Result<List<ReportPeriodVO>> monthly(@RequestParam(required = false) String year) {
        String sql = "SELECT DATE_FORMAT(create_time, '%Y-%m') as period, " +
                     "COUNT(*) as orderCount, " +
                     "COALESCE(SUM(total_amount), 0) as revenue, " +
                     "COALESCE(SUM(points_earned), 0) as points " +
                     "FROM consumption_order " +
                     "WHERE (? IS NULL OR YEAR(create_time) = ?) " +
                     "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
                     "ORDER BY period DESC";
        return Result.success(jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(ReportPeriodVO.class), year, year));
    }

    @GetMapping("/service-rank")
    public Result<List<ServiceRankVO>> serviceRank() {
        String sql = "SELECT i.item_name as itemName, COUNT(*) as count, " +
                     "COALESCE(SUM(i.item_price * i.quantity), 0) as revenue " +
                     "FROM consumption_order_item i " +
                     "GROUP BY i.item_name " +
                     "ORDER BY count DESC LIMIT 10";
        return Result.success(jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(ServiceRankVO.class)));
    }

    @GetMapping("/cashier-daily")
    public Result<List<CashierDailyVO>> cashierDaily(@RequestParam(required = false) String date) {
        String sql = "SELECT " +
                     "pay_method as payMethod, " +
                     "COUNT(*) as orderCount, " +
                     "COALESCE(SUM(total_amount), 0) as totalAmount, " +
                     "COALESCE(SUM(pay_amount), 0) as payAmount, " +
                     "COALESCE(SUM(balance_used), 0) as balanceUsed " +
                     "FROM consumption_order " +
                     "WHERE (? IS NULL OR DATE(create_time) = ?) " +
                     "AND status = 1 " +
                     "GROUP BY pay_method " +
                     "ORDER BY totalAmount DESC";
        return Result.success(jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(CashierDailyVO.class), date, date));
    }

    @GetMapping("/member-spending")
    public Result<List<MemberSpendingVO>> memberSpending(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "10") int limit) {
        String sql = "SELECT m.id, m.name, " +
                     "CONCAT(LEFT(m.phone, 3), '****', RIGHT(m.phone, 4)) as phone, " +
                     "COUNT(o.id) as orderCount, " +
                     "COALESCE(SUM(o.total_amount), 0) as totalSpent " +
                     "FROM member m " +
                     "LEFT JOIN consumption_order o ON o.member_id = m.id AND o.status = 1 " +
                     "AND (? IS NULL OR o.create_time >= ?) " +
                     "AND (? IS NULL OR o.create_time <= ?) " +
                     "GROUP BY m.id, m.name, m.phone " +
                     "HAVING orderCount > 0 " +
                     "ORDER BY totalSpent DESC LIMIT ?";
        return Result.success(jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(MemberSpendingVO.class), start, start, end, end, limit));
    }

    @GetMapping("/employee-performance")
    public Result<List<EmployeePerformanceVO>> employeePerformance(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        String sql = "SELECT e.id, e.name, " +
                     "COUNT(DISTINCT o.id) as orderCount, " +
                     "COALESCE(SUM(oi.item_price * oi.quantity), 0) as totalRevenue, " +
                     "COUNT(oi.id) as serviceCount, " +
                     "COALESCE(SUM(o.commission_amount), 0) as totalCommission " +
                     "FROM employee e " +
                     "LEFT JOIN consumption_order o ON o.employee_id = e.id " +
                     "AND o.create_time >= ? AND o.create_time <= ? AND o.status = 1 " +
                     "LEFT JOIN consumption_order_item oi ON oi.order_id = o.id " +
                     "WHERE e.status = 1 " +
                     "GROUP BY e.id, e.name " +
                     "ORDER BY totalRevenue DESC";
        String startDate = start != null ? start : "1970-01-01";
        String endDate = end != null ? end : "2099-12-31";
        return Result.success(jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(EmployeePerformanceVO.class), startDate, endDate));
    }

    @GetMapping("/member-trend")
    public Result<List<MemberTrendVO>> memberTrend() {
        String sql = """
            SELECT DATE_FORMAT(create_time, '%Y-%m') AS period,
                   COUNT(*) AS newMembers
            FROM member
            WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
            GROUP BY DATE_FORMAT(create_time, '%Y-%m')
            ORDER BY period ASC
            """;
        return Result.success(jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(MemberTrendVO.class)));
    }

    @GetMapping("/coupon-usage")
    public Result<List<CouponUsageVO>> couponUsage() {
        String sql = """
            SELECT ct.name AS templateName,
                   ct.issued_qty AS issuedQty,
                   COALESCE(u.used_qty, 0) AS usedQty,
                   CASE WHEN ct.issued_qty > 0
                        THEN ROUND(COALESCE(u.used_qty, 0) * 100.0 / ct.issued_qty, 1)
                        ELSE 0 END AS usageRate
            FROM coupon_template ct
            LEFT JOIN (
                SELECT template_id, COUNT(*) AS used_qty
                FROM coupon
                WHERE status >= 2
                GROUP BY template_id
            ) u ON u.template_id = ct.id
            ORDER BY usageRate DESC
            """;
        return Result.success(jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(CouponUsageVO.class)));
    }
}
