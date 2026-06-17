package com.salon.employee.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.employee.dto.EmployeeReqDTO;
import com.salon.employee.entity.Employee;
import com.salon.employee.service.EmployeeService;
import com.salon.employee.vo.EmployeeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public Result<PageResult<EmployeeVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Employee::getStatus, status);
        }
        Page<Employee> p = employeeService.page(new Page<>(page, pageSize), wrapper);
        return Result.success(PageResult.of(p, EmployeeVO::from));
    }

    @GetMapping("/{id}")
    public Result<EmployeeVO> getById(@PathVariable Long id) {
        return Result.success(EmployeeVO.from(employeeService.getById(id)));
    }

    @PostMapping
    public Result<EmployeeVO> create(@Valid @RequestBody EmployeeReqDTO req) {
        Employee employee = new Employee();
        employee.setName(req.getName());
        employee.setPhone(req.getPhone());
        employee.setPosition(req.getPosition());
        employee.setSalary(req.getSalary());
        employee.setStatus(req.getStatus());
        employeeService.save(employee);
        return Result.success(EmployeeVO.from(employee));
    }

    @PutMapping("/{id}")
    public Result<EmployeeVO> update(@PathVariable Long id, @Valid @RequestBody EmployeeReqDTO req) {
        Employee employee = employeeService.getById(id);
        if (employee == null) {
            return Result.notFound("员工不存在");
        }
        employee.setName(req.getName());
        employee.setPhone(req.getPhone());
        employee.setPosition(req.getPosition());
        employee.setSalary(req.getSalary());
        employee.setStatus(req.getStatus());
        employeeService.updateById(employee);
        return Result.success(EmployeeVO.from(employee));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.removeById(id);
        return Result.success();
    }
}
