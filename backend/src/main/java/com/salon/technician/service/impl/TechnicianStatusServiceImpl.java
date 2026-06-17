package com.salon.technician.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.employee.entity.Employee;
import com.salon.employee.mapper.EmployeeMapper;
import com.salon.technician.entity.TechnicianStatus;
import com.salon.technician.mapper.TechnicianStatusMapper;
import com.salon.technician.service.TechnicianStatusService;
import com.salon.technician.vo.TechnicianStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TechnicianStatusServiceImpl extends ServiceImpl<TechnicianStatusMapper, TechnicianStatus> implements TechnicianStatusService {

    private final EmployeeMapper employeeMapper;
    private final JdbcTemplate jdbcTemplate;

    private static final Set<String> VALID_STATUSES = Set.of("AVAILABLE", "BUSY", "BREAK", "OFF_DUTY");

    @Override
    public List<TechnicianStatusVO> listWithEmployee() {
        List<Employee> employees = employeeMapper.selectList(
            new LambdaQueryWrapper<Employee>().eq(Employee::getStatus, 1));

        List<TechnicianStatusVO> result = new ArrayList<>();
        for (Employee emp : employees) {
            TechnicianStatusVO vo = new TechnicianStatusVO();
            vo.setEmployeeId(emp.getId());
            vo.setEmployeeName(emp.getName());
            vo.setPhone(emp.getPhone());
            vo.setPosition(emp.getPosition());

            TechnicianStatus ts = getOne(
                new LambdaQueryWrapper<TechnicianStatus>().eq(TechnicianStatus::getEmployeeId, emp.getId()));
            if (ts != null) {
                vo.setStatus(ts.getStatus());
                vo.setCurrentCustomerName(ts.getCurrentCustomerName());
                vo.setCurrentServiceName(ts.getCurrentServiceName());
                vo.setLastStatusChanged(ts.getLastStatusChanged());
            } else {
                vo.setStatus("OFF_DUTY");
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public TechnicianStatusVO getByEmployeeId(Long employeeId) {
        Employee emp = employeeMapper.selectById(employeeId);
        if (emp == null) return null;

        TechnicianStatusVO vo = new TechnicianStatusVO();
        vo.setEmployeeId(emp.getId());
        vo.setEmployeeName(emp.getName());
        TechnicianStatus ts = getOne(
            new LambdaQueryWrapper<TechnicianStatus>().eq(TechnicianStatus::getEmployeeId, employeeId));
        vo.setStatus(ts != null ? ts.getStatus() : "OFF_DUTY");
        vo.setCurrentCustomerName(ts != null ? ts.getCurrentCustomerName() : null);
        vo.setCurrentServiceName(ts != null ? ts.getCurrentServiceName() : null);
        return vo;
    }

    @Override
    @Transactional
    public void changeStatus(Long employeeId, String status, String currentCustomerName, String currentServiceName) {
        if (!VALID_STATUSES.contains(status)) {
            throw BusinessException.badRequest("无效的状态: " + status);
        }
        // 原子 upsert：存在则更新，不存在则插入
        int rows = jdbcTemplate.update(
            "INSERT INTO technician_status (employee_id, status, current_customer_name, current_service_name, last_status_changed) " +
            "VALUES (?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE status = VALUES(status), " +
            "current_customer_name = VALUES(current_customer_name), " +
            "current_service_name = VALUES(current_service_name), " +
            "last_status_changed = VALUES(last_status_changed)",
            employeeId, status, currentCustomerName, currentServiceName, LocalDateTime.now());
        if (rows == 0) throw new BusinessException(ErrorCode.INTERNAL_ERROR, "技师状态更新失败");
        log.info("技师状态变更: employeeId={}, newStatus={}, customer={}, service={}",
            employeeId, status, currentCustomerName, currentServiceName);
    }
}
