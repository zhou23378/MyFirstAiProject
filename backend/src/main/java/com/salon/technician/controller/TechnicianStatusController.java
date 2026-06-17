package com.salon.technician.controller;

import com.salon.common.result.Result;
import com.salon.technician.dto.TechnicianStatusReqDTO;
import com.salon.technician.service.TechnicianStatusService;
import com.salon.technician.vo.TechnicianPerformanceVO;
import com.salon.technician.vo.TechnicianStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "技师状态", description = "技师实时状态看板")
@RestController
@RequestMapping("/api/technician-status")
@RequiredArgsConstructor
public class TechnicianStatusController {

    private final TechnicianStatusService technicianStatusService;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/list")
    @Operation(summary = "所有技师状态列表")
    public Result<List<TechnicianStatusVO>> list() {
        return Result.success(technicianStatusService.listWithEmployee());
    }

    @GetMapping("/{employeeId}")
    @Operation(summary = "单个技师状态")
    public Result<TechnicianStatusVO> getByEmployeeId(@PathVariable Long employeeId) {
        return Result.success(technicianStatusService.getByEmployeeId(employeeId));
    }

    @PostMapping("/change")
    @Operation(summary = "变更技师状态")
    public Result<Void> changeStatus(@Valid @RequestBody TechnicianStatusReqDTO dto) {
        technicianStatusService.changeStatus(dto.getEmployeeId(), dto.getStatus(),
            dto.getCurrentCustomerName(), dto.getCurrentServiceName());
        return Result.success();
    }

    @GetMapping("/performance")
    @Operation(summary = "技师今日业绩统计")
    public Result<List<TechnicianPerformanceVO>> performance() {
        String sql = """
            SELECT e.id, e.name,
                   COUNT(o.id) AS today_orders,
                   COALESCE(SUM(o.total_amount), 0) AS today_revenue
            FROM employee e
            LEFT JOIN consumption_order o ON o.employee_id = e.id
                AND o.status = 1
                AND DATE(o.create_time) = CURDATE()
            WHERE e.status = 1
            GROUP BY e.id, e.name
            ORDER BY today_revenue DESC
            """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        List<TechnicianPerformanceVO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            TechnicianPerformanceVO vo = new TechnicianPerformanceVO();
            vo.setEmployeeId((Long) row.get("id"));
            vo.setEmployeeName((String) row.get("name"));
            vo.setTodayOrders((Long) row.get("today_orders"));
            vo.setTodayRevenue((BigDecimal) row.get("today_revenue"));
            result.add(vo);
        }
        return Result.success(result);
    }
}
