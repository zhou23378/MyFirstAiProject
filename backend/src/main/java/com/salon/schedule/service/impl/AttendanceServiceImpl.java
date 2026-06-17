package com.salon.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.schedule.entity.Attendance;
import com.salon.schedule.entity.Schedule;
import com.salon.schedule.mapper.AttendanceMapper;
import com.salon.schedule.mapper.ScheduleMapper;
import com.salon.schedule.service.AttendanceService;
import com.salon.schedule.vo.AttendancePageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl extends ServiceImpl<AttendanceMapper, Attendance> implements AttendanceService {

    private final JdbcTemplate jdbcTemplate;
    private final ScheduleMapper scheduleMapper;

    @Override
    public Page<AttendancePageVO> pageWithNames(int page, int pageSize, String date, Long employeeId) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder();

        if (date != null) {
            where.append(" AND a.date = ?");
            params.add(date);
        }
        if (employeeId != null) {
            where.append(" AND a.employee_id = ?");
            params.add(employeeId);
        }

        String countSql = "SELECT COUNT(*) FROM attendance a WHERE 1=1" + where;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        int offset = (page - 1) * pageSize;
        params.add(offset);
        params.add(pageSize);
        String dataSql = "SELECT a.id, a.employee_id AS employeeId, a.schedule_id AS scheduleId, " +
                "a.date, a.clock_in AS clockIn, a.clock_out AS clockOut, " +
                "a.status, a.remark, a.create_time AS createTime, a.update_time AS updateTime, " +
                "e.name AS employeeName " +
                "FROM attendance a " +
                "LEFT JOIN employee e ON a.employee_id = e.id " +
                "WHERE 1=1" + where +
                " ORDER BY a.create_time DESC LIMIT ?, ?";
        List<AttendancePageVO> list = jdbcTemplate.query(dataSql, new BeanPropertyRowMapper<>(AttendancePageVO.class), params.toArray());

        Page<AttendancePageVO> result = new Page<>(page, pageSize);
        result.setTotal(total != null ? total : 0);
        result.setRecords(list);
        return result;
    }

    @Override
    @Transactional
    public Attendance clock(Long employeeId, Integer type) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        if (Integer.valueOf(1).equals(type)) {
            // Clock in — ensure row exists, then atomically set clock_in
            Long scheduleId = null;
            Integer status = 1; // normal
            Schedule schedule = scheduleMapper.selectOne(new LambdaQueryWrapper<Schedule>()
                    .eq(Schedule::getEmployeeId, employeeId)
                    .eq(Schedule::getDate, today));
            if (schedule != null) {
                scheduleId = schedule.getId();
                LocalTime shiftStart = jdbcTemplate.queryForObject(
                        "SELECT start_time FROM shift_template WHERE id = ?",
                        LocalTime.class, schedule.getShiftId());
                if (shiftStart != null && now.toLocalTime().isAfter(shiftStart)) {
                    status = 2; // late
                }
            }
            // INSERT IGNORE: 今日已存在记录时跳过（rows=0 为预期行为）
            int insertRows = jdbcTemplate.update(
                "INSERT IGNORE INTO attendance (employee_id, date, schedule_id, status) VALUES (?, ?, ?, ?)",
                employeeId, today, scheduleId, status);
            // INSERT IGNORE: rows=0 表示今日已有记录（预期行为）
            // Atomic UPDATE: only succeeds if clock_in is still NULL
            int rows = jdbcTemplate.update(
                "UPDATE attendance SET clock_in = ?, status = ? WHERE employee_id = ? AND date = ? AND clock_in IS NULL",
                now, status, employeeId, today);
            if (rows == 0) throw new BusinessException(ErrorCode.BAD_REQUEST, "今日已打卡上班");
        } else {
            // Clock out — atomic UPDATE
            int rows = jdbcTemplate.update(
                "UPDATE attendance SET clock_out = ? WHERE employee_id = ? AND date = ? " +
                "AND clock_in IS NOT NULL AND clock_out IS NULL",
                now, employeeId, today);
            if (rows == 0) throw new BusinessException(ErrorCode.BAD_REQUEST, "请先打卡上班或今日已打卡下班");
        }

        return getOne(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getEmployeeId, employeeId)
                .eq(Attendance::getDate, today));
    }
}
