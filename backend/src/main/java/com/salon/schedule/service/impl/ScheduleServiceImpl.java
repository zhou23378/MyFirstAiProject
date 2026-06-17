package com.salon.schedule.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.schedule.entity.Schedule;
import com.salon.schedule.mapper.ScheduleMapper;
import com.salon.schedule.service.ScheduleService;
import com.salon.schedule.vo.ScheduleWeekVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<ScheduleWeekVO> weekView(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT s.id, s.employee_id AS employeeId, s.shift_id AS shiftId, s.date, " +
                "e.name AS employeeName, st.name AS shiftName, st.start_time AS startTime, " +
                "st.end_time AS endTime, st.color " +
                "FROM schedule s " +
                "JOIN employee e ON s.employee_id = e.id " +
                "JOIN shift_template st ON s.shift_id = st.id " +
                "WHERE s.date BETWEEN ? AND ? " +
                "ORDER BY s.date, e.name";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ScheduleWeekVO.class), startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSet(Long employeeId, List<Schedule> schedules) {
        // Remove existing schedules for this employee in the date range
        for (Schedule s : schedules) {
            LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<Schedule>()
                    .eq(Schedule::getEmployeeId, employeeId)
                    .eq(Schedule::getDate, s.getDate());
            remove(wrapper);
        }
        for (Schedule s : schedules) {
            s.setEmployeeId(employeeId);
            save(s);
        }
    }
}
