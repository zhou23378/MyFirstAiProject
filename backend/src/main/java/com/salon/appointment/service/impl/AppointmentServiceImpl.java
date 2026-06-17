package com.salon.appointment.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.appointment.dto.ConvertToOrderDTO;
import com.salon.appointment.entity.Appointment;
import com.salon.appointment.mapper.AppointmentMapper;
import com.salon.appointment.service.AppointmentService;
import com.salon.appointment.vo.AppointmentPageVO;
import com.salon.appointment.vo.CalendarAppointmentVO;
import com.salon.appointment.vo.CalendarDayVO;
import com.salon.appointment.vo.CalendarWeekVO;
import com.salon.appointment.vo.TechnicianCalendarVO;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.common.result.Result;
import com.salon.consumption.dto.CreateOrderDTO;
import com.salon.consumption.entity.ConsumptionOrder;
import com.salon.consumption.service.ConsumptionOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private final JdbcTemplate jdbcTemplate;

    @Deprecated(since = "微服务迁移后移除")
    private final ConsumptionOrderService consumptionOrderService;

    @Override
    public Page<AppointmentPageVO> pageWithNames(int page, int pageSize, String startDate, String endDate, Integer status) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder();

        if (startDate != null) {
            where.append(" AND a.appointment_date >= ?");
            params.add(startDate);
        }
        if (endDate != null) {
            where.append(" AND a.appointment_date <= ?");
            params.add(endDate);
        }
        if (status != null) {
            where.append(" AND a.status = ?");
            params.add(status);
        }

        String countSql = "SELECT COUNT(*) FROM appointment a WHERE 1=1" + where;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        int offset = (page - 1) * pageSize;
        params.add(offset);
        params.add(pageSize);
        String dataSql = "SELECT a.id, a.member_id AS memberId, a.service_item_id AS serviceItemId, " +
                "a.employee_id AS employeeId, a.appointment_date AS appointmentDate, " +
                "a.start_time AS startTime, a.end_time AS endTime, a.duration, " +
                "a.status, a.remark, a.create_time AS createTime, a.update_time AS updateTime, " +
                "m.name AS memberName, si.name AS serviceItemName, e.name AS employeeName " +
                "FROM appointment a " +
                "LEFT JOIN member m ON a.member_id = m.id " +
                "LEFT JOIN service_item si ON a.service_item_id = si.id " +
                "LEFT JOIN employee e ON a.employee_id = e.id " +
                "WHERE 1=1" + where +
                " ORDER BY a.appointment_date DESC, a.start_time ASC LIMIT ?, ?";
        List<AppointmentPageVO> list = jdbcTemplate.query(dataSql, new BeanPropertyRowMapper<>(AppointmentPageVO.class), params.toArray());

        Page<AppointmentPageVO> result = new Page<>(page, pageSize);
        result.setTotal(total != null ? total : 0);
        result.setRecords(list);
        return result;
    }

    @Override
    public void checkTimeConflict(Long employeeId, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeId) {
        if (employeeId == null || date == null || startTime == null || endTime == null) return;
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "开始时间必须早于结束时间");
        }

        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT id, start_time, end_time FROM appointment " +
            "WHERE employee_id = ? AND appointment_date = ? AND status IN (1, 2) " +
            "AND start_time < ? AND end_time > ?");
        params.add(employeeId);
        params.add(date);
        params.add(endTime);
        params.add(startTime);

        if (excludeId != null) {
            sql.append(" AND id != ?");
            params.add(excludeId);
        }

        List<Map<String, Object>> conflicts = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        if (!conflicts.isEmpty()) {
            Map<String, Object> conflict = conflicts.get(0);
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                String.format("该技师此时间段已忙碌（%s - %s 已有预约）",
                    conflict.get("start_time"), conflict.get("end_time")));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> convertToOrder(Long appointmentId, ConvertToOrderDTO dto) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null) {
            return Result.notFound("预约不存在");
        }
        Integer apptStatus = appointment.getStatus();
        if (!Integer.valueOf(1).equals(apptStatus) && !Integer.valueOf(2).equals(apptStatus)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前预约状态无法转为消费");
        }

        // 查询服务项目信息
        Map<String, Object> serviceItem = null;
        if (appointment.getServiceItemId() != null) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, name, price FROM service_item WHERE id = ?", appointment.getServiceItemId());
            if (!rows.isEmpty()) serviceItem = rows.get(0);
        }

        // 构建 CreateOrderDTO
        CreateOrderDTO orderDTO = new CreateOrderDTO();
        orderDTO.setMemberId(appointment.getMemberId());
        orderDTO.setPayMethod(dto.getPayMethod());
        orderDTO.setPayRemark(dto.getPayRemark());
        orderDTO.setEmployeeId(appointment.getEmployeeId());

        if (serviceItem != null) {
            CreateOrderDTO.OrderItemDTO item = new CreateOrderDTO.OrderItemDTO();
            item.setItemId(appointment.getServiceItemId());
            item.setItemName((String) serviceItem.get("name"));
            item.setItemPrice(new BigDecimal(serviceItem.get("price").toString()));
            item.setQuantity(1);
            orderDTO.setItems(List.of(item));
        } else {
            throw new BusinessException(ErrorCode.SERVICE_NOT_FOUND);
        }

        // 余额支付由 createOrder 根据折扣后金额统一计算
        ConsumptionOrder order = consumptionOrderService.createOrder(orderDTO);

        log.info("预约转消费: appointmentId={}, orderId={}, totalAmount={}, memberId={}",
            appointmentId, order.getId(), order.getTotalAmount(), appointment.getMemberId());

        // 原子更新预约状态为已完成
        int apptRows = jdbcTemplate.update(
            "UPDATE appointment SET status = 3 WHERE id = ? AND status IN (1, 2)", appointmentId);
        if (apptRows == 0) throw new BusinessException(ErrorCode.BAD_REQUEST, "预约状态已变更，无法转为消费");

        return Result.success(Map.of("orderId", order.getId(), "totalAmount", order.getTotalAmount()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void arrive(Long appointmentId) {
        // 原子更新：只有 status=1 的预约才能到店
        int apptRows = jdbcTemplate.update(
            "UPDATE appointment SET status = 2 WHERE id = ? AND status = 1", appointmentId);
        if (apptRows == 0) throw new BusinessException(ErrorCode.BAD_REQUEST, "当前预约状态不可到店");

        Long memberId = jdbcTemplate.queryForObject(
            "SELECT member_id FROM appointment WHERE id = ?", Long.class, appointmentId);
        int rows = jdbcTemplate.update(
            "UPDATE member SET last_visit_date = ?, visit_count = visit_count + 1 WHERE id = ?",
            java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")), memberId);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "到店更新会员数据失败：会员不存在");
        }
        log.info("预约到店: appointmentId={}, memberId={}", appointmentId, memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, int newStatus) {
        // 原子状态校验 + 更新：WHERE 条件同时校验旧状态和合法转换
        int rows = jdbcTemplate.update(
            "UPDATE appointment SET status = ? WHERE id = ? AND (" +
            "(status = 1 AND ? IN (2, 4, 5)) OR " +
            "(status = 2 AND ? IN (3, 5)))",
            newStatus, id, newStatus, newStatus);
        if (rows == 0) throw new BusinessException(ErrorCode.BAD_REQUEST, "预约不存在或状态变更无效");
    }

    @Override
    public CalendarDayVO getCalendarDay(String date, Long employeeId, Integer status) {
        // 1. 查询所有在职技师 + 实时状态
        String techSql = "SELECT e.id, e.name, COALESCE(ts.status, 'OFF_DUTY') AS status " +
                "FROM employee e LEFT JOIN technician_status ts ON e.id = ts.employee_id " +
                "WHERE e.status = 1 ORDER BY e.id";
        List<Map<String, Object>> techRows = jdbcTemplate.queryForList(techSql);

        // 2. 查询当天预约（含会员名、服务名）
        List<Object> params = new ArrayList<>();
        StringBuilder aptWhere = new StringBuilder("WHERE a.appointment_date = ?");
        params.add(date);
        if (employeeId != null) {
            aptWhere.append(" AND a.employee_id = ?");
            params.add(employeeId);
        }
        if (status != null) {
            aptWhere.append(" AND a.status = ?");
            params.add(status);
        }

        String aptSql = "SELECT a.id, a.member_id AS memberId, m.name AS memberName, " +
                "a.service_item_id AS serviceItemId, si.name AS serviceItemName, " +
                "a.employee_id AS employeeId, " +
                "TIME_FORMAT(a.start_time, '%H:%i') AS startTime, " +
                "TIME_FORMAT(a.end_time, '%H:%i') AS endTime, " +
                "a.status, a.remark " +
                "FROM appointment a " +
                "LEFT JOIN member m ON a.member_id = m.id " +
                "LEFT JOIN service_item si ON a.service_item_id = si.id " +
                aptWhere +
                " ORDER BY a.start_time ASC";
        List<Map<String, Object>> aptRows = jdbcTemplate.queryForList(aptSql, params.toArray());

        // 3. 按技师分组
        Map<Long, List<CalendarAppointmentVO>> grouped = new java.util.LinkedHashMap<>();
        List<CalendarAppointmentVO> unassigned = new ArrayList<>();
        for (Map<String, Object> row : aptRows) {
            CalendarAppointmentVO vo = new CalendarAppointmentVO();
            vo.setId(toLong(row.get("id")));
            vo.setMemberId(toLong(row.get("memberId")));
            vo.setMemberName((String) row.get("memberName"));
            vo.setServiceItemId(toLong(row.get("serviceItemId")));
            vo.setServiceItemName((String) row.get("serviceItemName"));
            vo.setStartTime((String) row.get("startTime"));
            vo.setEndTime((String) row.get("endTime"));
            vo.setStatus((Integer) row.get("status"));
            vo.setRemark((String) row.get("remark"));

            Long empId = toLong(row.get("employeeId"));
            if (empId != null) {
                grouped.computeIfAbsent(empId, k -> new ArrayList<>()).add(vo);
            } else {
                unassigned.add(vo);
            }
        }

        // 4. 组装技师列表
        List<TechnicianCalendarVO> technicians = new ArrayList<>();
        for (Map<String, Object> tech : techRows) {
            TechnicianCalendarVO tvo = new TechnicianCalendarVO();
            tvo.setId(toLong(tech.get("id")));
            tvo.setName((String) tech.get("name"));
            tvo.setStatus((String) tech.get("status"));
            tvo.setAppointments(grouped.getOrDefault(tvo.getId(), List.of()));
            technicians.add(tvo);
        }

        // 5. 统计
        Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        stats.put("total", aptRows.size());
        int booked = 0, arrived = 0, completed = 0;
        for (Map<String, Object> row : aptRows) {
            Integer s = (Integer) row.get("status");
            if (Integer.valueOf(1).equals(s)) booked++;
            else if (Integer.valueOf(2).equals(s)) arrived++;
            else if (Integer.valueOf(3).equals(s)) completed++;
        }
        stats.put("booked", booked);
        stats.put("arrived", arrived);
        stats.put("completed", completed);

        CalendarDayVO result = new CalendarDayVO();
        result.setTechnicians(technicians);
        result.setUnassignedAppointments(unassigned);
        result.setStats(stats);
        result.setBusinessHours(Map.of("start", "08:00", "end", "21:00"));
        return result;
    }

    @Override
    public CalendarWeekVO getCalendarWeek(String startDate, Long employeeId) {
        // 计算7天范围
        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = start.plusDays(6);

        String[] weekLabels = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        // 整周预约查询
        List<Object> params = new ArrayList<>();
        StringBuilder aptWhere = new StringBuilder("WHERE a.appointment_date >= ? AND a.appointment_date <= ?");
        params.add(startDate);
        params.add(end.toString());
        if (employeeId != null) {
            aptWhere.append(" AND a.employee_id = ?");
            params.add(employeeId);
        }

        String aptSql = "SELECT a.id, a.member_id AS memberId, m.name AS memberName, " +
                "a.service_item_id AS serviceItemId, si.name AS serviceItemName, " +
                "a.appointment_date AS appointmentDate, " +
                "TIME_FORMAT(a.start_time, '%H:%i') AS startTime, " +
                "TIME_FORMAT(a.end_time, '%H:%i') AS endTime, " +
                "a.status, a.remark " +
                "FROM appointment a " +
                "LEFT JOIN member m ON a.member_id = m.id " +
                "LEFT JOIN service_item si ON a.service_item_id = si.id " +
                aptWhere +
                " ORDER BY a.appointment_date ASC, a.start_time ASC";
        List<Map<String, Object>> aptRows = jdbcTemplate.queryForList(aptSql, params.toArray());

        // 按日期分组
        Map<String, List<Map<String, Object>>> groupedByDate = new java.util.LinkedHashMap<>();
        for (Map<String, Object> row : aptRows) {
            String day = row.get("appointmentDate").toString();
            groupedByDate.computeIfAbsent(day, k -> new ArrayList<>()).add(row);
        }

        // 组装7天
        List<CalendarWeekVO.CalendarWeekDayVO> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            java.time.LocalDate day = start.plusDays(i);
            String dateStr = day.toString();

            CalendarWeekVO.CalendarWeekDayVO dayVO = new CalendarWeekVO.CalendarWeekDayVO();
            dayVO.setDate(dateStr);
            dayVO.setDayOfWeek(weekLabels[i]);

            List<Map<String, Object>> dayApts = groupedByDate.getOrDefault(dateStr, List.of());
            dayVO.setTotalCount(dayApts.size());

            Map<String, Integer> statusCounts = new java.util.LinkedHashMap<>();
            statusCounts.put("booked", 0);
            statusCounts.put("arrived", 0);
            statusCounts.put("completed", 0);
            statusCounts.put("cancelled", 0);
            statusCounts.put("noshow", 0);
            for (Map<String, Object> row : dayApts) {
                Integer s = (Integer) row.get("status");
                if (Integer.valueOf(1).equals(s)) statusCounts.merge("booked", 1, Integer::sum);
                else if (Integer.valueOf(2).equals(s)) statusCounts.merge("arrived", 1, Integer::sum);
                else if (Integer.valueOf(3).equals(s)) statusCounts.merge("completed", 1, Integer::sum);
                else if (Integer.valueOf(4).equals(s)) statusCounts.merge("cancelled", 1, Integer::sum);
                else if (Integer.valueOf(5).equals(s)) statusCounts.merge("noshow", 1, Integer::sum);
            }
            dayVO.setStatusCounts(statusCounts);

            // 最多4条预览
            List<CalendarAppointmentVO> previews = new ArrayList<>();
            for (int j = 0; j < Math.min(dayApts.size(), 4); j++) {
                Map<String, Object> row = dayApts.get(j);
                CalendarAppointmentVO vo = new CalendarAppointmentVO();
                vo.setId(toLong(row.get("id")));
                vo.setMemberId(toLong(row.get("memberId")));
                vo.setMemberName((String) row.get("memberName"));
                vo.setServiceItemId(toLong(row.get("serviceItemId")));
                vo.setServiceItemName((String) row.get("serviceItemName"));
                vo.setStartTime((String) row.get("startTime"));
                vo.setEndTime((String) row.get("endTime"));
                vo.setStatus((Integer) row.get("status"));
                vo.setRemark((String) row.get("remark"));
                previews.add(vo);
            }
            dayVO.setAppointments(previews);
            days.add(dayVO);
        }

        CalendarWeekVO result = new CalendarWeekVO();
        result.setDays(days);
        return result;
    }

    private static Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        return Long.valueOf(val.toString());
    }
}
